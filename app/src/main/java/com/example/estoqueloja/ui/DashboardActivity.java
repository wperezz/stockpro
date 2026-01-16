package com.example.estoqueloja.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.estoqueloja.data.db.DbProvider;
import com.example.estoqueloja.data.entity.Produto;
import com.example.estoqueloja.databinding.ActivityDashboardBinding;
import com.example.estoqueloja.util.AppExecutors;
import com.example.estoqueloja.util.InsetsUtil;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        InsetsUtil.applyPaddingSystemBarsTopBottom(b.getRoot());
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarAsync();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void carregarAsync() {

        // Calcula períodos na UI mesmo (isso é leve)
        // Hoje: 00:00 até amanhã 00:00
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long iniHoje = c.getTimeInMillis();
        long fimHoje = iniHoje + 24L * 60 * 60 * 1000;

        // Mês: 1º dia 00:00 até 1º dia do próximo mês 00:00
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

            // Estoque baixo
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
}
