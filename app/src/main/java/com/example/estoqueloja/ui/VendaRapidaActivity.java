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

    // produto selecionado no dropdown (controlado)
    private Produto produtoSelecionado;

    // carrinho
    private final List<com.example.estoqueloja.ui.model.VendaItem> carrinho = new ArrayList<>();
    private com.example.estoqueloja.ui.adapter.VendaCarrinhoAdapter carrinhoAdapter;

    private TextView txtTotalGeral, txtLucroGeral;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityVendaRapidaBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        InsetsUtil.applyPaddingSystemBarsTopBottom(b.getRoot());

        acProdutos = findViewById(R.id.acProdutos);
        txtInfoProduto = findViewById(R.id.txtInfoProduto);
        edQtd = findViewById(R.id.edQtd);
        edObs = findViewById(R.id.edObs);

        produtoIdInicial = getIntent().getLongExtra("produtoId", 0);

        txtTotalVenda = findViewById(R.id.txtTotalVenda);
        txtLucroVenda = findViewById(R.id.txtLucroVenda);

        txtTotalGeral = findViewById(R.id.txtTotalGeral);
        txtLucroGeral = findViewById(R.id.txtLucroGeral);

        // Recycler carrinho
        androidx.recyclerview.widget.RecyclerView recycler = findViewById(R.id.recyclerCarrinho);
        recycler.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        carrinhoAdapter = new com.example.estoqueloja.ui.adapter.VendaCarrinhoAdapter(
                new com.example.estoqueloja.ui.adapter.VendaCarrinhoAdapter.Callback() {
                    @Override public void onMais(int pos) { aumentarQtd(pos); }
                    @Override public void onMenos(int pos) { diminuirQtd(pos); }
                    @Override public void onRemover(int pos) { removerItem(pos); }
                    @Override public void onEditarQtd(int pos) { editarQtdDialog(pos); }
                }
        );

        recycler.setAdapter(carrinhoAdapter);

        findViewById(R.id.btnAdicionar).setOnClickListener(v -> adicionarItem());
        findViewById(R.id.btnFinalizar).setOnClickListener(v -> finalizarVenda());
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        acProdutos.setOnClickListener(v -> acProdutos.showDropDown());
        acProdutos.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) acProdutos.showDropDown();
        });

        // Se o usuário digitar/apagar manualmente, invalida a seleção pra não vender produto errado
        acProdutos.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Se o texto não bate com o produto selecionado, desmarca
                if (produtoSelecionado != null) {
                    String t = (s == null ? "" : s.toString());
                    if (!t.equals(produtoSelecionado.nome)) {
                        produtoSelecionado = null;
                        atualizarInfo();
                    }
                } else {
                    // sem produto selecionado: só atualiza os totais locais
                    atualizarTotaisLocal();
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        edQtd.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                atualizarTotaisLocal();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        TextView titulo = findViewById(R.id.txtTitulo);
        titulo.setText("Venda");

        carregarProdutos();
        atualizarTotaisCarrinho();
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
                        acProdutos.setText(pIni.nome, false);
                    } else {
                        produtoSelecionado = null;
                        acProdutos.setText("", false);
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
        for (Produto p : produtos) if (p.id == id) return p;
        return null;
    }

    private Produto acharProdutoPorNome(String nome) {
        if (nome == null) return null;
        for (Produto p : produtos) if (nome.equals(p.nome)) return p;
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

    // quantidade total do produto no carrinho
    private int qtdNoCarrinho(long produtoId) {
        int soma = 0;
        for (com.example.estoqueloja.ui.model.VendaItem it : carrinho) {
            if (it.produto.id == produtoId) soma += it.qtd;
        }
        return soma;
    }

    private void adicionarItem() {
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

        // valida estoque já considerando o carrinho
        AppExecutors.get().io().execute(() -> {
            int estoqueAtual = DbProvider.get(this).movDao().estoqueAtual(p.id);
            int jaNoCarrinho = qtdNoCarrinho(p.id);
            int totalDesejado = jaNoCarrinho + qtd;

            if (totalDesejado > estoqueAtual) {
                AppExecutors.get().main().post(() ->
                        Toast.makeText(this,
                                "Estoque insuficiente. Atual: " + estoqueAtual + " | No carrinho: " + jaNoCarrinho,
                                Toast.LENGTH_LONG).show());
                return;
            }

            AppExecutors.get().main().post(() -> {
                // soma se já existe
                boolean achou = false;
                for (com.example.estoqueloja.ui.model.VendaItem it : carrinho) {
                    if (it.produto.id == p.id) {
                        it.qtd += qtd;
                        achou = true;
                        break;
                    }
                }
                if (!achou) carrinho.add(new com.example.estoqueloja.ui.model.VendaItem(p, qtd));

                carrinhoAdapter.setItens(carrinho);
                atualizarTotaisCarrinho();

                // limpa campos para agilizar
                edQtd.setText("");
                edObs.setText("");

                // opcional: limpa seleção pra facilitar adicionar outro produto
                produtoSelecionado = null;
                acProdutos.setText("", false);
                atualizarInfo();

                Toast.makeText(this, "Item adicionado na venda.", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void atualizarTotaisCarrinho() {
        double total = 0;
        double lucro = 0;
        for (com.example.estoqueloja.ui.model.VendaItem it : carrinho) {
            total += it.total();
            lucro += it.lucro();
        }
        txtTotalGeral.setText(String.format(Locale.getDefault(), "Total Geral: R$ %.2f", total));
        txtLucroGeral.setText(String.format(Locale.getDefault(), "Lucro Geral: R$ %.2f", lucro));
    }

    private void finalizarVenda() {
        if (carrinho.isEmpty()) {
            Toast.makeText(this, "Adicione itens na venda.", Toast.LENGTH_LONG).show();
            return;
        }

        final String obs = edObs.getText() == null ? "" : edObs.getText().toString().trim();

        AppExecutors.get().io().execute(() -> {
            // valida estoque de cada produto
            for (com.example.estoqueloja.ui.model.VendaItem it : carrinho) {
                Produto p = it.produto;

                if (p.ativo == 0) {
                    AppExecutors.get().main().post(() ->
                            Toast.makeText(this, "Há produto inativo na venda: " + p.nome, Toast.LENGTH_LONG).show());
                    return;
                }

                int estoque = DbProvider.get(this).movDao().estoqueAtual(p.id);
                if (it.qtd > estoque) {
                    AppExecutors.get().main().post(() ->
                            Toast.makeText(this, "Estoque insuficiente para " + p.nome + ". Atual: " + estoque,
                                    Toast.LENGTH_LONG).show());
                    return;
                }
            }

            // grava em transação (tudo ou nada)
            DbProvider.get(this).runInTransaction(() -> {
                for (com.example.estoqueloja.ui.model.VendaItem it : carrinho) {
                    Produto p = it.produto;

                    MovEstoque m = new MovEstoque();
                    m.produtoId = p.id;
                    m.tipo = "SAIDA";
                    m.quantidade = it.qtd;
                    m.dataHora = System.currentTimeMillis();
                    m.obs = obs;
                    m.custoUnit = p.custoAtual;
                    m.precoUnit = p.precoVenda;

                    DbProvider.get(this).movDao().inserir(m);
                }
            });

            AppExecutors.get().main().post(() -> {
                Toast.makeText(this, "Venda finalizada!", Toast.LENGTH_SHORT).show();

                carrinho.clear();
                carrinhoAdapter.setItens(carrinho);
                atualizarTotaisCarrinho();

                produtoSelecionado = null;
                acProdutos.setText("", false);
                atualizarInfo();
            });
        });
    }

    private void aumentarQtd(int pos) {
        if (pos < 0 || pos >= carrinho.size()) return;

        com.example.estoqueloja.ui.model.VendaItem it = carrinho.get(pos);
        Produto p = it.produto;

        AppExecutors.get().io().execute(() -> {
            int estoqueAtual = DbProvider.get(this).movDao().estoqueAtual(p.id);

            if (it.qtd + 1 > estoqueAtual) {
                AppExecutors.get().main().post(() ->
                        Toast.makeText(this,
                                "Estoque insuficiente. Atual: " + estoqueAtual,
                                Toast.LENGTH_LONG).show());
                return;
            }

            it.qtd += 1;

            AppExecutors.get().main().post(() -> {
                carrinhoAdapter.setItens(carrinho);
                atualizarTotaisCarrinho();
            });
        });
    }

    private void diminuirQtd(int pos) {
        if (pos < 0 || pos >= carrinho.size()) return;

        com.example.estoqueloja.ui.model.VendaItem it = carrinho.get(pos);
        it.qtd -= 1;

        if (it.qtd <= 0) {
            carrinho.remove(pos);
        }

        carrinhoAdapter.setItens(carrinho);
        atualizarTotaisCarrinho();
    }

    private void removerItem(int pos) {
        if (pos < 0 || pos >= carrinho.size()) return;
        carrinho.remove(pos);
        carrinhoAdapter.setItens(carrinho);
        atualizarTotaisCarrinho();
    }

    private void editarQtdDialog(int pos) {
        if (pos < 0 || pos >= carrinho.size()) return;

        com.example.estoqueloja.ui.model.VendaItem it = carrinho.get(pos);
        Produto p = it.produto;

        final android.widget.EditText ed = new android.widget.EditText(this);
        ed.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        ed.setText(String.valueOf(it.qtd));
        ed.setSelection(ed.getText().length());

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Quantidade - " + p.nome)
                .setMessage("Digite a nova quantidade:")
                .setView(ed)
                .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                .setNeutralButton("Remover", (d, w) -> {
                    carrinho.remove(pos);
                    carrinhoAdapter.setItens(carrinho);
                    atualizarTotaisCarrinho();
                })
                .setPositiveButton("OK", (d, w) -> {
                    int novaQtd = parseInt(ed.getText() == null ? "" : ed.getText().toString());
                    if (novaQtd <= 0) {
                        carrinho.remove(pos);
                        carrinhoAdapter.setItens(carrinho);
                        atualizarTotaisCarrinho();
                        return;
                    }

                    // valida estoque no background
                    AppExecutors.get().io().execute(() -> {
                        int estoqueAtual = DbProvider.get(this).movDao().estoqueAtual(p.id);

                        if (novaQtd > estoqueAtual) {
                            AppExecutors.get().main().post(() ->
                                    Toast.makeText(this,
                                            "Estoque insuficiente. Atual: " + estoqueAtual,
                                            Toast.LENGTH_LONG).show());
                            return;
                        }

                        it.qtd = novaQtd;

                        AppExecutors.get().main().post(() -> {
                            carrinhoAdapter.setItens(carrinho);
                            atualizarTotaisCarrinho();
                        });
                    });
                })
                .show();
    }

}
