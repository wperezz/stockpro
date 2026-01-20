package com.example.estoqueloja.ui;

import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.estoqueloja.R;
import com.example.estoqueloja.data.dao.TopVendidoRow;
import com.example.estoqueloja.data.db.DbProvider;
import com.example.estoqueloja.databinding.ActivityProdutoExtratoBinding;
import com.example.estoqueloja.databinding.ActivityTopVendidosBinding;
import com.example.estoqueloja.ui.adapter.TopVendidosAdapter;
import com.example.estoqueloja.util.AppExecutors;
import com.example.estoqueloja.util.InsetsUtil;

import java.util.Calendar;
import java.util.List;

public class TopVendidosActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private TopVendidosAdapter adapter;
    private RadioGroup rg;
    private ActivityTopVendidosBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityTopVendidosBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        InsetsUtil.applyPaddingSystemBarsTopBottom(b.getRoot());
        recycler = findViewById(R.id.recyclerTop);
        rg = findViewById(R.id.rgPeriodo);

        adapter = new TopVendidosAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        rg.setOnCheckedChangeListener((group, checkedId) -> carregar());
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        TextView titulo = findViewById(R.id.txtTitulo);
        titulo.setText("Top Vendidos");

    }

    @Override
    protected void onResume() {
        super.onResume();
        carregar();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void carregar() {
        AppExecutors.get().io().execute(() -> {
            long[] range = calcularPeriodo();
            long ini = range[0];
            long fim = range[1];

            List<TopVendidoRow> rows = DbProvider.get(this).movDao().topVendidosValidos(ini, fim, 30);

            AppExecutors.get().main().post(() -> adapter.setItens(rows));
        });
    }

    private long[] calcularPeriodo() {
        Calendar c = Calendar.getInstance();
        long fim = c.getTimeInMillis();

        int checked = rg.getCheckedRadioButtonId();
        if (checked == R.id.rbHoje) {
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            long ini = c.getTimeInMillis();
            long fimHoje = ini + 24L * 60 * 60 * 1000;
            return new long[]{ini, fimHoje};
        }

        int dias = (checked == R.id.rb7) ? 7 : 30;
        c.add(Calendar.DAY_OF_MONTH, -dias);
        long ini = c.getTimeInMillis();
        return new long[]{ini, fim};
    }
}

