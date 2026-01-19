package com.example.estoqueloja.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.estoqueloja.R;
import com.example.estoqueloja.data.db.DbProvider;
import com.example.estoqueloja.data.entity.MovEstoque;
import com.example.estoqueloja.data.entity.Produto;
import com.example.estoqueloja.databinding.ActivityVendaRapidaBinding;
import com.example.estoqueloja.util.AppExecutors;
import com.example.estoqueloja.util.InsetsUtil;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VendaRapidaActivity extends AppCompatActivity {

    private AutoCompleteTextView acProdutos;
    private TextView txtInfoProduto;
    private TextInputEditText edQtd, edObs;

    private List<Produto> produtos = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private long produtoIdInicial = 0;
    private ActivityVendaRapidaBinding b;

    private TextView txtTotalVenda, txtLucroVenda;

    // controla o produto selecionado no dropdown
    private Produto produtoSelecionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityVendaRapidaBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        InsetsUtil.applyPaddingSystemBarsTopBottom(b.getRoot());

        acProdutos = findViewById(R.id.acProdutos);
        txtInfoProduto = findViewById(R.id.txtInfoProduto);
        edQtd = findViewById(R.id.edQtd);
        edObs = findViewById(R.id.edObs);

        produtoIdInicial = getIntent().getLongExtra("produtoId", 0);

        findViewById(R.id.btnConfirmar).setOnClickListener(v -> confirmarVenda());

        findViewById(R.id.btnVenderOutra).setOnClickListener(v -> {
            edQtd.setText("");
            edObs.setText("");
            acProdutos.requestFocus();
            // não limpa seleção automaticamente (fica a seu critério)
            atualizarTotaisLocal();
        });

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        acProdutos.setOnClickListener(v -> acProdutos.showDropDown());

        TextView titulo = findViewById(R.id.txtTitulo);
        titulo.setText("Venda");

        txtTotalVenda = findViewById(R.id.txtTotalVenda);
        txtLucroVenda = findViewById(R.id.txtLucroVenda);

        edQtd.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                atualizarTotaisLocal();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        carregarProdutos();
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
                adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, nomes);
                acProdutos.setAdapter(adapter);
                acProdutos.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) acProdutos.showDropDown();
                });
                // quando escolhe um item da lista
                acProdutos.setOnItemClickListener((parent, view, position, id) -> {
                    String nome = (String) parent.getItemAtPosition(position);
                    produtoSelecionado = acharProdutoPorNome(nome);
                    atualizarInfo();
                });

                // seleção inicial por ID (se veio de outra tela)
                if (produtoIdInicial > 0) {
                    Produto pIni = acharProdutoPorId(produtoIdInicial);
                    if (pIni != null) {
                        produtoSelecionado = pIni;
                        acProdutos.setText(pIni.nome, false); // false = não dispara filtro
                    }
                } else {
                    produtoSelecionado = null;
                    acProdutos.setText("", false);
                }

                atualizarInfo();
            });
        });
    }

    private Produto acharProdutoPorId(long id) {
        for (Produto p : produtos) {
            if (p.id == id) return p;
        }
        return null;
    }

    private Produto acharProdutoPorNome(String nome) {
        if (nome == null) return null;
        for (Produto p : produtos) {
            if (nome.equals(p.nome)) return p;
        }
        return null;
    }

    private void atualizarInfo() {
        Produto p = produtoSelecionado;
        if (p == null) {
            txtInfoProduto.setText("Selecione um produto");
            atualizarTotaisLocal();
            return;
        }

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
        Produto p = produtoSelecionado;

        if (p == null) {
            Toast.makeText(this, "Selecione um produto.", Toast.LENGTH_LONG).show();
            return;
        }

        int qtd = parseInt(edQtd.getText() == null ? "" : edQtd.getText().toString());
        if (qtd <= 0) {
            edQtd.setError("Informe a quantidade");
            return;
        }

        String obs = edObs.getText() == null ? "" : edObs.getText().toString().trim();

        AppExecutors.get().io().execute(() -> {
            if (p.ativo == 0) {
                AppExecutors.get().main().post(() ->
                        Toast.makeText(this, "Produto inativo. Reative para vender.", Toast.LENGTH_LONG).show());
                return;
            }

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
        Produto p = produtoSelecionado;
        if (p == null) {
            txtTotalVenda.setText("Total: R$ 0,00");
            txtLucroVenda.setText("Lucro: R$ 0,00");
            return;
        }

        int qtd = parseInt(edQtd.getText() == null ? "" : edQtd.getText().toString());
        if (qtd < 0) qtd = 0;

        double total = p.precoVenda * qtd;
        double lucro = (p.precoVenda - p.custoAtual) * qtd;

        txtTotalVenda.setText(String.format(Locale.getDefault(), "Total: R$ %.2f", total));
        txtLucroVenda.setText(String.format(Locale.getDefault(), "Lucro: R$ %.2f", lucro));
    }
}