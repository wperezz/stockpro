package com.example.estoqueloja.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.estoqueloja.R;
import com.example.estoqueloja.data.db.DbProvider;
import com.example.estoqueloja.ui.adapter.VendaDiaAdapter;
import com.example.estoqueloja.util.AppExecutors;
import com.example.estoqueloja.util.InsetsUtil;

import java.util.Calendar;
import java.util.Locale;

public class HistoricoDiaActivity extends AppCompatActivity {

    private VendaDiaAdapter adapter;
    private TextView txtResumo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico_dia);
        InsetsUtil.applyPaddingSystemBarsTopBottom(findViewById(android.R.id.content));

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        ((TextView)findViewById(R.id.txtTitulo)).setText("Histórico do Dia");

        txtResumo = findViewById(R.id.txtResumo);

        androidx.recyclerview.widget.RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VendaDiaAdapter();
        recycler.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregar();
    }

    private void carregar() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long ini = c.getTimeInMillis();
        long fim = ini + 24L * 60 * 60 * 1000;

        AppExecutors.get().io().execute(() -> {
            double total = DbProvider.get(this).movDao().totalVendasPeriodo(ini, fim);
            double lucro = DbProvider.get(this).movDao().lucroVendasPeriodo(ini, fim);
            java.util.List<com.example.estoqueloja.data.dao.VendaDiaRow> rows =
                    DbProvider.get(this).movDao().vendasDoDia(ini, fim);

            AppExecutors.get().main().post(() -> {
                txtResumo.setText(String.format(Locale.getDefault(),
                        "Total: R$ %.2f | Lucro: R$ %.2f", total, lucro));
                adapter.setItens(rows);
            });
        });
    }
}
