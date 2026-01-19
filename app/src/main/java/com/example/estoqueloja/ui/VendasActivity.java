package com.example.estoqueloja.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.estoqueloja.R;
import com.example.estoqueloja.data.db.DbProvider;
import com.example.estoqueloja.databinding.ActivityVendasBinding;
import com.example.estoqueloja.ui.adapter.VendasAdapter;
import com.example.estoqueloja.util.AppExecutors;
import com.example.estoqueloja.util.InsetsUtil;

public class VendasActivity extends AppCompatActivity {

    private ActivityVendasBinding b;
    private VendasAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityVendasBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        InsetsUtil.applyPaddingSystemBarsTopBottom(b.getRoot());

        TextView titulo = findViewById(R.id.txtTitulo);
        titulo.setText("Vendas");

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        adapter = new VendasAdapter(venda -> {
            Intent it = new Intent(this, VendaDetalheActivity.class);
            it.putExtra("vendaId", venda.vendaId);
            startActivity(it);
        });

        b.recyclerVendas.setLayoutManager(new LinearLayoutManager(this));
        b.recyclerVendas.setAdapter(adapter);
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
            java.util.List<com.example.estoqueloja.data.dao.VendaResumoRow> lista =
                    DbProvider.get(this).movDao().listarVendasAgrupadas();

            AppExecutors.get().main().post(() -> adapter.setItens(lista));
        });
    }
}
