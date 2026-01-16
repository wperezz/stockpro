package com.example.estoqueloja.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.estoqueloja.R;
import com.example.estoqueloja.data.db.DbProvider;
import com.example.estoqueloja.data.entity.MovEstoque;
import com.example.estoqueloja.data.entity.Produto;
import com.example.estoqueloja.databinding.ActivityMovimentoBinding;
import com.example.estoqueloja.databinding.ActivityProdutoExtratoBinding;
import com.example.estoqueloja.ui.adapter.MovimentoAdapter;
import com.example.estoqueloja.util.AppExecutors;
import com.example.estoqueloja.util.InsetsUtil;

import java.util.List;
import java.util.Locale;

public class ProdutoExtratoActivity extends AppCompatActivity {

    private long produtoId;
    private TextView txtNome, txtInfo, txtResumo;
    private RecyclerView recycler;
    private MovimentoAdapter adapter;
    private ActivityProdutoExtratoBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityProdutoExtratoBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        InsetsUtil.applyPaddingSystemBarsTopBottom(b.getRoot());

        produtoId = getIntent().getLongExtra("id", 0);

        txtNome = findViewById(R.id.txtNome);
        txtInfo = findViewById(R.id.txtInfo);
        txtResumo = findViewById(R.id.txtResumo);
        recycler = findViewById(R.id.recyclerExtrato);

        adapter = new MovimentoAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        findViewById(R.id.btnEntrada).setOnClickListener(v -> abrirMovimento("ENTRADA"));
        findViewById(R.id.btnSaida).setOnClickListener(v -> abrirMovimento("SAIDA"));
        findViewById(R.id.btnEditar).setOnClickListener(v -> {
            Intent it = new Intent(this, ProdutoFormActivity.class);
            it.putExtra("id", produtoId);
            startActivity(it);
        });

        findViewById(R.id.btnVender).setOnClickListener(v -> {
            Intent it = new Intent(this, VendaRapidaActivity.class);
            it.putExtra("produtoId", produtoId);
            startActivity(it);
        });
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        TextView titulo = findViewById(R.id.txtTitulo);
        titulo.setText("Extrato Produto");

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
            Produto p = DbProvider.get(this).produtoDao().obter(produtoId);
            int estoque = DbProvider.get(this).movDao().estoqueAtual(produtoId);
            List<MovEstoque> movsAsc = DbProvider.get(this).movDao().listarPorProdutoAsc(produtoId);

            AppExecutors.get().main().post(() -> {
                if (p == null) {
                    finish();
                    return;
                }
                txtNome.setText(p.nome);
                txtInfo.setText((p.categoria == null ? "" : p.categoria));

                double lucroUnit = p.precoVenda - p.custoAtual;
                txtResumo.setText(String.format(Locale.getDefault(),
                        "Estoque: %d | Mín: %d | Lucro unit: R$ %.2f",
                        estoque, p.estoqueMinimo, lucroUnit));

                adapter.setDados(movsAsc);
            });
        });
    }

    private void abrirMovimento(String tipo) {
        Intent it = new Intent(this, MovimentoActivity.class);
        it.putExtra("produtoId", produtoId);
        it.putExtra("tipo", tipo);
        startActivity(it);
    }

}
