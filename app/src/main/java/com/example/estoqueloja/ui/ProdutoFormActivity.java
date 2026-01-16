package com.example.estoqueloja.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.estoqueloja.R;
import com.example.estoqueloja.data.db.DbProvider;
import com.example.estoqueloja.data.entity.Produto;
import com.example.estoqueloja.databinding.ActivityProdutoFormBinding;
import com.example.estoqueloja.util.AppExecutors;
import com.example.estoqueloja.util.InsetsUtil;

public class ProdutoFormActivity extends AppCompatActivity {

    private ActivityProdutoFormBinding b;
    private long id = 0;
    private Produto produto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = ActivityProdutoFormBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        InsetsUtil.applyPaddingSystemBarsTopBottom(b.getRoot());

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        id = getIntent().getLongExtra("id", 0);

        // default: cadastro
        ((TextView) findViewById(R.id.txtTitulo)).setText("Cadastrar Produto");

        b.btnSalvar.setOnClickListener(v -> salvar());
        b.btnExcluir.setOnClickListener(v -> confirmarExcluir());
        b.btnAtivarInativar.setOnClickListener(v -> confirmarAtivarInativar());

        // se veio id, carregar no background (não pode Room na main thread)
        if (id > 0) carregarProdutoAsync(id);
    }

    private void carregarProdutoAsync(long id) {
        AppExecutors.get().io().execute(() -> {
            Produto p = DbProvider.get(this).produtoDao().obter(id);

            AppExecutors.get().main().post(() -> {
                produto = p;
                b.btnAtivarInativar.setVisibility(android.view.View.VISIBLE);
                atualizarTextoBotaoAtivo();

                if (produto != null) {
                    ((TextView) findViewById(R.id.txtTitulo)).setText("Editar Produto");
                    b.btnExcluir.setVisibility(android.view.View.VISIBLE);
                    preencher();
                } else {
                    Toast.makeText(this, "Produto não encontrado", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }

    private void preencher() {
        b.edNome.setText(produto.nome);
        b.edCategoria.setText(produto.categoria);
        b.edCusto.setText(String.valueOf(produto.custoAtual));
        b.edPreco.setText(String.valueOf(produto.precoVenda));
        b.edMinimo.setText(String.valueOf(produto.estoqueMinimo));
    }

    private void salvar() {
        String nome = b.edNome.getText().toString().trim();
        if (nome.isEmpty()) {
            b.edNome.setError("Informe o nome");
            return;
        }

        String categoria = b.edCategoria.getText().toString().trim();
        double custo = parseDouble(b.edCusto.getText().toString());
        double preco = parseDouble(b.edPreco.getText().toString());
        int minimo = (int) parseDouble(b.edMinimo.getText().toString());

        final boolean editando = (produto != null);

        if (produto == null) produto = new Produto(nome);
        produto.nome = nome;
        produto.categoria = categoria;
        produto.custoAtual = custo;
        produto.precoVenda = preco;
        produto.estoqueMinimo = minimo;

        AppExecutors.get().io().execute(() -> {
            try {
                if (editando) {
                    DbProvider.get(this).produtoDao().atualizar(produto);
                } else {
                    DbProvider.get(this).produtoDao().inserir(produto);
                }

                AppExecutors.get().main().post(() -> {
                    Toast.makeText(this, "Produto salvo com sucesso", Toast.LENGTH_SHORT).show();
                    finish();
                });

            } catch (Exception e) {
                AppExecutors.get().main().post(() ->
                        Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void confirmarExcluir() {
        if (produto == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Excluir produto")
                .setMessage("Tem certeza que deseja excluir este produto?\n\nSe ele tiver movimentações, não será possível excluir.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (d, w) -> excluirAsync())
                .show();
    }

    private void excluirAsync() {
        if (produto == null) return;

        AppExecutors.get().io().execute(() -> {
            try {
                // Segurança: não deixar excluir se tiver movimentações
                int movs = DbProvider.get(this).movDao().countMovsDoProduto(produto.id);
                if (movs > 0) {
                    AppExecutors.get().main().post(() ->
                            Toast.makeText(this, "Não é possível excluir: produto possui movimentações.", Toast.LENGTH_LONG).show());
                    return;
                }

                DbProvider.get(this).produtoDao().deletar(produto);

                AppExecutors.get().main().post(() -> {
                    Toast.makeText(this, "Produto excluído", Toast.LENGTH_SHORT).show();
                    finish();
                });

            } catch (Exception e) {
                AppExecutors.get().main().post(() ->
                        Toast.makeText(this, "Erro ao excluir: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private double parseDouble(String s) {
        if (s == null) return 0;
        s = s.trim().replace(",", ".");
        if (s.isEmpty()) return 0;
        try { return Double.parseDouble(s); }
        catch (Exception e) { return 0; }
    }

    private void atualizarTextoBotaoAtivo() {
        if (produto == null) return;
        b.btnAtivarInativar.setText(
                produto.ativo == 1 ? "Inativar produto" : "Reativar produto"
        );
    }

    private void confirmarAtivarInativar() {
        if (produto == null) return;

        final boolean vaiAtivar = produto.ativo == 0;

        new AlertDialog.Builder(this)
                .setTitle(vaiAtivar ? "Reativar produto" : "Inativar produto")
                .setMessage(vaiAtivar
                        ? "Deseja reativar este produto?"
                        : "Deseja inativar este produto?\n\nEle não aparecerá nas telas de venda e movimentação.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton(vaiAtivar ? "Reativar" : "Inativar",
                        (d, w) -> setAtivoAsync(vaiAtivar))
                .show();
    }

    private void setAtivoAsync(boolean ativar) {
        AppExecutors.get().io().execute(() -> {
            try {
                int novoStatus = ativar ? 1 : 0;
                DbProvider.get(this).produtoDao().setAtivo(produto.id, novoStatus);
                produto.ativo = novoStatus;

                AppExecutors.get().main().post(() -> {
                    atualizarTextoBotaoAtivo();
                    Toast.makeText(
                            this,
                            ativar ? "Produto reativado" : "Produto inativado",
                            Toast.LENGTH_SHORT
                    ).show();
                });

            } catch (Exception e) {
                AppExecutors.get().main().post(() ->
                        Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

}
