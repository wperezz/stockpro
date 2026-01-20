package com.example.estoqueloja.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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


    private RadioButton rbEntrada, rbSaida;
    private TextInputEditText edQtd, edObs;

    private List<Produto> produtos = new ArrayList<>();

    private long produtoIdInicial = 0;
    private String tipoInicial = null;
    private ActivityMovimentoBinding b;
    private TextView txtEstoqueAtual;

    private AutoCompleteTextView acProdutos;
    private ArrayAdapter<String> adapterNomes;

    // produto selecionado controlado
    private Produto produtoSelecionado;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = ActivityMovimentoBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        InsetsUtil.applyPaddingSystemBarsTopBottom(b.getRoot());
        acProdutos = findViewById(R.id.acProdutos);

        acProdutos.setOnClickListener(v -> acProdutos.showDropDown());
        acProdutos.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) acProdutos.showDropDown();
        });
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

                adapterNomes = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, nomes);
                acProdutos.setAdapter(adapterNomes);

                acProdutos.setOnItemClickListener((parent, view, position, id) -> {
                    String nome = (String) parent.getItemAtPosition(position);
                    produtoSelecionado = acharProdutoPorNome(nome);
                    atualizarEstoqueSelecionadoAsync();
                });

                // se o usuário digitar/apagar manualmente, invalida seleção
                acProdutos.addTextChangedListener(new android.text.TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (produtoSelecionado != null) {
                            String t = (s == null ? "" : s.toString());
                            if (!t.equals(produtoSelecionado.nome)) {
                                produtoSelecionado = null;
                                txtEstoqueAtual.setText("0");
                            }
                        }
                    }
                    @Override public void afterTextChanged(android.text.Editable s) {}
                });

                // aplica seleção inicial por ID (se veio de outra tela)
                if (produtoIdInicial > 0) {
                    Produto pIni = acharProdutoPorId(produtoIdInicial);
                    if (pIni != null) {
                        produtoSelecionado = pIni;
                        acProdutos.setText(pIni.nome, false);
                    } else {
                        produtoSelecionado = null;
                        acProdutos.setText("", false);
                    }
                } else {
                    produtoSelecionado = null;
                    acProdutos.setText("", false);
                }

                // aplica tipo inicial, se existir
                if ("SAIDA".equals(tipoInicial)) rbSaida.setChecked(true);
                else rbEntrada.setChecked(true);

                atualizarEstoqueSelecionadoAsync();
            });
        });
    }

    private Produto acharProdutoPorNome(String nome) {
        if (nome == null) return null;
        for (Produto p : produtos) if (nome.equals(p.nome)) return p;
        return null;
    }

    private Produto acharProdutoPorId(long id) {
        if (id <= 0) return null;
        for (Produto p : produtos) {
            if (p.id == id) return p;
        }
        return null;
    }

    private void salvarMovAsync() {

        if (produtos.isEmpty()) {
            Toast.makeText(this, "Cadastre um produto primeiro", Toast.LENGTH_SHORT).show();
            return;
        }

        Produto p = produtoSelecionado;
        if (p == null) {
            Toast.makeText(this, "Selecione um produto.", Toast.LENGTH_LONG).show();
            return;
        }

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
                setResult(RESULT_OK);
                finish();
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
        Produto p = produtoSelecionado;
        if (p == null) {
            txtEstoqueAtual.setText("0");
            return;
        }

        AppExecutors.get().io().execute(() -> {
            int estoque = DbProvider.get(this).movDao().estoqueAtual(p.id);
            AppExecutors.get().main().post(() -> txtEstoqueAtual.setText(String.valueOf(estoque)));
        });
    }

}
