package com.example.estoqueloja.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.estoqueloja.R;
import com.example.estoqueloja.data.db.DbProvider;
import com.example.estoqueloja.data.entity.Produto;
import com.example.estoqueloja.databinding.ActivityMainBinding;
import com.example.estoqueloja.util.AppExecutors;
import com.example.estoqueloja.util.CsvExporter;
import com.example.estoqueloja.util.InsetsUtil;
import com.google.android.material.navigation.NavigationView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding b;

    private DrawerLayout drawer;
    private NavigationView nav;
    private ActionBarDrawerToggle toggle;

    private static final int REQ_EXPORT = 1001;

    // 0=validas, 1=canceladas, 2=todas
    private int filtroDashboard = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        // Insets (se o seu InsetsUtil já resolve top/bottom, mantém)
        //InsetsUtil.applyPaddingSystemBarsTopBottom(b.contentMain);

        // Toolbar
        setSupportActionBar(b.toolbar);

        // Drawer
        drawer = b.drawer;
        nav = b.navView;

        toggle = new ActionBarDrawerToggle(
                this, drawer, b.toolbar,
                R.string.app_name, R.string.app_name
        );
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // Botão principal
        b.btnVendaRapida.setOnClickListener(v ->
                startActivity(new Intent(this, VendaRapidaActivity.class)));

        // Toggle filtro (dashboard)
        b.tgFiltro.check(R.id.btnFiltroValidas); // padrão: válidas

        b.tgFiltro.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            if (checkedId == R.id.btnFiltroValidas) filtroDashboard = 0;
            else if (checkedId == R.id.btnFiltroCanceladas) filtroDashboard = 1;
            else if (checkedId == R.id.btnFiltroTodas) filtroDashboard = 2;

            carregarDashboardAsync();
        });

        // Menu lateral
        nav.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            item.setChecked(true);

            if (id == R.id.nav_produtos) {
                startActivity(new Intent(this, ProdutosActivity.class));
            } else if (id == R.id.nav_movimento) {
                startActivity(new Intent(this, MovimentoActivity.class));
            } else if (id == R.id.nav_vendas) {
                startActivity(new Intent(this, VendasActivity.class));
            } else if (id == R.id.nav_top) {
                startActivity(new Intent(this, TopVendidosActivity.class));
            } else if (id == R.id.nav_exportar) {
                abrirExportarCsv();
            }

            drawer.closeDrawers();
            return true;
        });

        // Back fecha drawer primeiro
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (drawer != null && drawer.isDrawerOpen(nav)) {
                    drawer.closeDrawers();
                } else {
                    setEnabled(false);
                    MainActivity.super.onBackPressed();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarDashboardAsync();
    }

    private void abrirExportarCsv() {
        Intent it = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        it.addCategory(Intent.CATEGORY_OPENABLE);
        it.setType("text/csv");
        it.putExtra(Intent.EXTRA_TITLE, "estoque_loja.csv");
        startActivityForResult(it, REQ_EXPORT);
    }

    private void carregarDashboardAsync() {
        // Hoje: [00:00, amanhã 00:00)
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long iniHoje = c.getTimeInMillis();
        long fimHoje = iniHoje + 24L * 60 * 60 * 1000;

        // Mês: [1º dia 00:00, 1º dia do próximo mês 00:00)
        Calendar m = Calendar.getInstance();
        m.set(Calendar.DAY_OF_MONTH, 1);
        m.set(Calendar.HOUR_OF_DAY, 0);
        m.set(Calendar.MINUTE, 0);
        m.set(Calendar.SECOND, 0);
        m.set(Calendar.MILLISECOND, 0);
        long iniMes = m.getTimeInMillis();
        m.add(Calendar.MONTH, 1);
        long fimMes = m.getTimeInMillis();

        AppExecutors.get().io().execute(() -> {
            double fatHoje = DbProvider.get(this).movDao().faturamentoFiltrado(iniHoje, fimHoje, filtroDashboard);
            double lucroHoje = DbProvider.get(this).movDao().lucroFiltrado(iniHoje, fimHoje, filtroDashboard);

            double fatMes = DbProvider.get(this).movDao().faturamentoFiltrado(iniMes, fimMes, filtroDashboard);
            double lucroMes = DbProvider.get(this).movDao().lucroFiltrado(iniMes, fimMes, filtroDashboard);

            List<Produto> produtos = DbProvider.get(this).produtoDao().listarAtivos();
            int baixo = 0;
            for (Produto p : produtos) {
                int est = DbProvider.get(this).movDao().estoqueAtual(p.id);
                if (est <= p.estoqueMinimo) baixo++;
            }

            int finalBaixo = baixo;
            AppExecutors.get().main().post(() -> {
                b.txtFatHoje.setText(String.format(Locale.getDefault(), "R$ %.2f", fatHoje));
                b.txtLucroHoje.setText(String.format(Locale.getDefault(), "R$ %.2f", lucroHoje));
                b.txtFatMes.setText(String.format(Locale.getDefault(), "R$ %.2f", fatMes));
                b.txtLucroMes.setText(String.format(Locale.getDefault(), "R$ %.2f", lucroMes));
                b.txtEstoqueBaixo.setText(String.valueOf(finalBaixo));
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_EXPORT && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri == null) return;

            AppExecutors.get().io().execute(() -> {
                try {
                    CsvExporter.exportarTudo(this, uri);
                    AppExecutors.get().main().post(() ->
                            Toast.makeText(this, "CSV exportado com sucesso!", Toast.LENGTH_LONG).show());
                } catch (Exception e) {
                    AppExecutors.get().main().post(() ->
                            Toast.makeText(this, "Erro ao exportar: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle != null && toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (toggle != null) toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (toggle != null) toggle.onConfigurationChanged(newConfig);
    }


}
