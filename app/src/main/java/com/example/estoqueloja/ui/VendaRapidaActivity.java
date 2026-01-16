package com.example.estoqueloja.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.estoqueloja.R;
import com.example.estoqueloja.data.db.DbProvider;
import com.example.estoqueloja.data.entity.MovEstoque;
import com.example.estoqueloja.data.entity.Produto;
import com.example.estoqueloja.databinding.ActivityTopVendidosBinding;
import com.example.estoqueloja.databinding.ActivityVendaRapidaBinding;
import com.example.estoqueloja.util.AppExecutors;
import com.example.estoqueloja.util.InsetsUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VendaRapidaActivity extends AppCompatActivity {

    private Spinner spProdutos;
    private TextView txtInfoProduto;
    private com.google.android.material.textfield.TextInputEditText edQtd, edObs;

    private List<Produto> produtos = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private long produtoIdInicial = 0;
    private ActivityVendaRapidaBinding b;

    private TextView txtTotalVenda, txtLucroVenda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityVendaRapidaBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        InsetsUtil.applyPaddingSystemBarsTopBottom(b.getRoot());
        spProdutos = findViewById(R.id.spProdutos);
        txtInfoProduto = findViewById(R.id.txtInfoProduto);
        edQtd = findViewById(R.id.edQtd);
        edObs = findViewById(R.id.edObs);

        produtoIdInicial = getIntent().getLongExtra("produtoId", 0);

        carregarProdutos();

        findViewById(R.id.btnConfirmar).setOnClickListener(v -> confirmarVenda());
        findViewById(R.id.btnVenderOutra).setOnClickListener(v -> {
            edQtd.setText("");
            edObs.setText("");
            spProdutos.requestFocus();
        });

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        TextView titulo = findViewById(R.id.txtTitulo);
        titulo.setText("Venda");

        txtTotalVenda = findViewById(R.id.txtTotalVenda);
        txtLucroVenda = findViewById(R.id.txtLucroVenda);

        edQtd.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                atualizarTotaisLocal();
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void carregarProdutos() {
        AppExecutors.get().io().execute(() -> {
            produtos = DbProvider.get(this).produtoDao().listarAtivos();
            List<String> nomes = new ArrayList<>();
            for (Produto p : produtos) nomes.add(p.nome);

            AppExecutors.get().main().post(() -> {
                adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, nomes);
                spProdutos.setAdapter(adapter);

                if (produtoIdInicial > 0) {
                    for (int i = 0; i < produtos.size(); i++) {
                        if (produtos.get(i).id == produtoIdInicial) {
                            spProdutos.setSelection(i);
                            break;
                        }
                    }
                }

                spProdutos.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                        atualizarInfo();
                    }
                    @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
                });

                atualizarInfo();
            });
        });
    }

    private void atualizarInfo() {
        if (produtos.isEmpty()) return;
        int idx = spProdutos.getSelectedItemPosition();
        if (idx < 0 || idx >= produtos.size()) return;

        Produto p = produtos.get(idx);

        AppExecutors.get().io().execute(() -> {
            int estoque = DbProvider.get(this).movDao().estoqueAtual(p.id);
            AppExecutors.get().main().post(() -> {
                txtInfoProduto.setText(String.format(Locale.getDefault(),
                        "Preço: R$ %.2f | Custo: R$ %.2f | Estoque: %d",
                        p.precoVenda, p.custoAtual, estoque));
                atualizarTotaisLocal();
            });
        });
    }

    private void confirmarVenda() {
        if (produtos.isEmpty()) {
            Toast.makeText(this, "Cadastre produtos primeiro.", Toast.LENGTH_LONG).show();
            return;
        }

        int idx = spProdutos.getSelectedItemPosition();
        if (idx < 0 || idx >= produtos.size()) return;

        Produto p = produtos.get(idx);

        int qtd = parseInt(edQtd.getText() == null ? "" : edQtd.getText().toString());
        if (qtd <= 0) {
            edQtd.setError("Informe a quantidade");
            return;
        }

        String obs = edObs.getText() == null ? "" : edObs.getText().toString().trim();

        AppExecutors.get().io().execute(() -> {
            int estoque = DbProvider.get(this).movDao().estoqueAtual(p.id);
            if (qtd > estoque) {
                AppExecutors.get().main().post(() ->
                        Toast.makeText(this, "Estoque insuficiente. Atual: " + estoque, Toast.LENGTH_LONG).show());
                return;
            }

            MovEstoque m = new MovEstoque();
            m.produtoId = p.id;
            m.tipo = "SAIDA";
            m.quantidade = qtd;
            m.dataHora = System.currentTimeMillis();
            m.obs = obs;
            m.custoUnit = p.custoAtual;
            m.precoUnit = p.precoVenda;

            if (p.ativo == 0) {
                Toast.makeText(this, "Produto inativo. Reative para vender.", Toast.LENGTH_LONG).show();
                return;
            }

            DbProvider.get(this).movDao().inserir(m);

            AppExecutors.get().main().post(() -> {
                Toast.makeText(this, "Venda registrada!", Toast.LENGTH_SHORT).show();
                atualizarInfo();
                edQtd.setText("");
                edObs.setText("");
            });
        });
    }

    private int parseInt(String s) {
        if (s == null) return 0;
        s = s.trim();
        if (s.isEmpty()) return 0;
        try { return Integer.parseInt(s); }
        catch (Exception e) { return 0; }
    }

    private void atualizarTotaisLocal() {
        if (produtos.isEmpty()) return;

        int idx = spProdutos.getSelectedItemPosition();
        if (idx < 0 || idx >= produtos.size()) return;

        Produto p = produtos.get(idx);

        int qtd = parseInt(edQtd.getText() == null ? "" : edQtd.getText().toString());
        if (qtd < 0) qtd = 0;

        double total = p.precoVenda * qtd;
        double lucro = (p.precoVenda - p.custoAtual) * qtd;

        txtTotalVenda.setText(String.format(Locale.getDefault(), "Total: R$ %.2f", total));
        txtLucroVenda.setText(String.format(Locale.getDefault(), "Lucro: R$ %.2f", lucro));
    }

}