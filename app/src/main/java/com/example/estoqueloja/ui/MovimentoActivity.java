package com.example.estoqueloja.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.estoqueloja.R;
import com.example.estoqueloja.data.db.DbProvider;
import com.example.estoqueloja.data.entity.MovEstoque;
import com.example.estoqueloja.data.entity.Produto;
import com.example.estoqueloja.databinding.ActivityDashboardBinding;
import com.example.estoqueloja.databinding.ActivityMovimentoBinding;
import com.example.estoqueloja.util.AppExecutors;
import com.example.estoqueloja.util.InsetsUtil;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class MovimentoActivity extends AppCompatActivity {

    private Spinner spProdutos;
    private RadioButton rbEntrada, rbSaida;
    private TextInputEditText edQtd, edObs;

    private List<Produto> produtos = new ArrayList<>();
    private ArrayAdapter<String> adapterNomes;

    private long produtoIdInicial = 0;
    private String tipoInicial = null;
    private ActivityMovimentoBinding b;
    private TextView txtEstoqueAtual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = ActivityMovimentoBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        InsetsUtil.applyPaddingSystemBarsTopBottom(b.getRoot());
        spProdutos = findViewById(R.id.spProdutos);
        rbEntrada = findViewById(R.id.rbEntrada);
        rbSaida = findViewById(R.id.rbSaida);
        edQtd = findViewById(R.id.edQtd);
        edObs = findViewById(R.id.edObs);

        // parâmetros opcionais (quando vem do extrato)
        produtoIdInicial = getIntent().getLongExtra("produtoId", 0);
        tipoInicial = getIntent().getStringExtra("tipo");

        // carregar spinner no background
        carregarProdutosAsync();

        findViewById(R.id.btnSalvarMov).setOnClickListener(v -> salvarMovAsync());
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        TextView titulo = findViewById(R.id.txtTitulo);
        titulo.setText("Movimento");

        txtEstoqueAtual = findViewById(R.id.txtEstoqueAtual);
        spProdutos.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                atualizarEstoqueSelecionadoAsync();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void carregarProdutosAsync() {
        AppExecutors.get().io().execute(() -> {
            List<Produto> lista = DbProvider.get(this).produtoDao().listarAtivos();

            List<String> nomes = new ArrayList<>();
            for (Produto p : lista) nomes.add(p.nome);

            AppExecutors.get().main().post(() -> {
                produtos.clear();
                produtos.addAll(lista);

                adapterNomes = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, nomes);
                spProdutos.setAdapter(adapterNomes);

                // aplica seleção inicial, se existir
                if (produtoIdInicial > 0) {
                    for (int i = 0; i < produtos.size(); i++) {
                        if (produtos.get(i).id == produtoIdInicial) {
                            spProdutos.setSelection(i);
                            break;
                        }
                    }
                }

                // aplica tipo inicial, se existir
                if ("SAIDA".equals(tipoInicial)) rbSaida.setChecked(true);
                else if ("ENTRADA".equals(tipoInicial)) rbEntrada.setChecked(true);
                else rbEntrada.setChecked(true);

                atualizarEstoqueSelecionadoAsync();
            });
        });
    }

    private void salvarMovAsync() {
        if (produtos.isEmpty()) {
            Toast.makeText(this, "Cadastre um produto primeiro", Toast.LENGTH_SHORT).show();
            return;
        }

        int idx = spProdutos.getSelectedItemPosition();
        if (idx < 0 || idx >= produtos.size()) return;

        Produto p = produtos.get(idx);

        if (p.ativo == 0) {
            Toast.makeText(this, "Produto inativo. Reative para movimentar.", Toast.LENGTH_LONG).show();
            return;
        }

        String tipo = rbEntrada.isChecked() ? "ENTRADA" : "SAIDA";
        int qtd = parseInt(edQtd.getText() == null ? "" : edQtd.getText().toString());

        if (qtd <= 0) {
            edQtd.setError("Informe a quantidade");
            return;
        }

        String obs = edObs.getText() == null ? "" : edObs.getText().toString().trim();

        AppExecutors.get().io().execute(() -> {
            // valida estoque na saída
            if ("SAIDA".equals(tipo)) {
                int estoque = DbProvider.get(this).movDao().estoqueAtual(p.id);
                if (qtd > estoque) {
                    AppExecutors.get().main().post(() ->
                            Toast.makeText(this, "Estoque insuficiente. Atual: " + estoque, Toast.LENGTH_LONG).show());
                    return;
                }
            }

            MovEstoque m = new MovEstoque();
            m.produtoId = p.id;
            m.tipo = tipo;
            m.quantidade = qtd;
            m.dataHora = System.currentTimeMillis();
            m.obs = obs;

            // grava custo/preço do momento (histórico de lucro correto)
            m.custoUnit = p.custoAtual;
            m.precoUnit = p.precoVenda;

            DbProvider.get(this).movDao().inserir(m);

            AppExecutors.get().main().post(() -> {
                Toast.makeText(this, "Movimentação salva", Toast.LENGTH_SHORT).show();
                edQtd.setText("");
                edObs.setText("");
                atualizarEstoqueSelecionadoAsync();
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

    private void atualizarEstoqueSelecionadoAsync() {
        if (produtos.isEmpty()) {
            txtEstoqueAtual.setText("0");
            return;
        }

        int idx = spProdutos.getSelectedItemPosition();
        if (idx < 0 || idx >= produtos.size()) return;

        Produto p = produtos.get(idx);

        AppExecutors.get().io().execute(() -> {
            int estoque = DbProvider.get(this).movDao().estoqueAtual(p.id);
            AppExecutors.get().main().post(() -> txtEstoqueAtual.setText(String.valueOf(estoque)));
        });
    }

}
