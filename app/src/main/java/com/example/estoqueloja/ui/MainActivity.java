package com.example.estoqueloja.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.estoqueloja.data.db.DbProvider;
import com.example.estoqueloja.data.entity.Produto;
import com.example.estoqueloja.databinding.ActivityMainBinding;
import com.example.estoqueloja.util.AppExecutors;
import com.example.estoqueloja.util.CsvExporter;
import com.example.estoqueloja.util.InsetsUtil;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding b;
    private static final int REQ_EXPORT = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        InsetsUtil.applyPaddingSystemBarsTopBottom(b.getRoot());

        b.btnProdutos.setOnClickListener(v ->
                startActivity(new Intent(this, ProdutosActivity.class)));

        b.btnMovimentar.setOnClickListener(v ->
                startActivity(new Intent(this, MovimentoActivity.class)));

        b.btnTop.setOnClickListener(v ->
                startActivity(new Intent(this, TopVendidosActivity.class)));

        b.btnVendas.setOnClickListener(v ->
                startActivity(new Intent(this, VendasActivity.class)));

        b.btnVendaRapida.setOnClickListener(v ->
                startActivity(new Intent(this, VendaRapidaActivity.class)));

        b.btnExportar.setOnClickListener(v -> {
            Intent it = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            it.addCategory(Intent.CATEGORY_OPENABLE);
            it.setType("text/csv");
            it.putExtra(Intent.EXTRA_TITLE, "estoque_loja.csv");
            startActivityForResult(it, REQ_EXPORT);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarDashboardAsync();
    }

    private void carregarDashboardAsync() {

        // Períodos (leve, pode ser na UI)
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long iniHoje = c.getTimeInMillis();
        long fimHoje = iniHoje + 24L * 60 * 60 * 1000;

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
            double fatHoje = DbProvider.get(this).movDao().faturamento(iniHoje, fimHoje);
            double lucroHoje = DbProvider.get(this).movDao().lucro(iniHoje, fimHoje);

            double fatMes = DbProvider.get(this).movDao().faturamento(iniMes, fimMes);
            double lucroMes = DbProvider.get(this).movDao().lucro(iniMes, fimMes);

            // estoque baixo (versão segura)
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
}
