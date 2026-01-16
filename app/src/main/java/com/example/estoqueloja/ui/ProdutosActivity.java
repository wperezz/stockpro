package com.example.estoqueloja.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.estoqueloja.R;
import com.example.estoqueloja.data.dao.EstoqueRow;
import com.example.estoqueloja.data.db.DbProvider;
import com.example.estoqueloja.databinding.ActivityProdutosBinding; // se você ainda usa binding
import com.example.estoqueloja.ui.adapter.ProdutoAdapter;
import com.example.estoqueloja.util.AppExecutors;
import com.example.estoqueloja.util.InsetsUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProdutosActivity extends AppCompatActivity {

    private ActivityProdutosBinding b;
    private ProdutoAdapter adapter;
    private String categoriaSelecionada = "Todas";
    private boolean categoriasCarregadas = false;
    private int filtroStatus = 1; // 1=Ativos, 0=Inativos, 2=Todos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityProdutosBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        InsetsUtil.applyPaddingSystemBarsTopBottom(b.getRoot());

        adapter = new ProdutoAdapter(
                // clique normal -> extrato
                p -> {
                    Intent it = new Intent(this, ProdutoExtratoActivity.class);
                    it.putExtra("id", p.id);
                    startActivity(it);
                },
                // clique longo -> editar
                p -> {
                    Intent it = new Intent(this, ProdutoFormActivity.class);
                    it.putExtra("id", p.id);
                    startActivity(it);
                }
        );


        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        b.recycler.setAdapter(adapter);

        // Spinner Status
        java.util.List<String> status = java.util.Arrays.asList("Ativos", "Inativos", "Todos");
        android.widget.ArrayAdapter<String> adStatus =
                new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, status);
        b.spStatus.setAdapter(adStatus);
        b.spStatus.setSelection(0); // Ativos padrão

        b.spStatus.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position == 0) filtroStatus = 1;       // Ativos
                else if (position == 1) filtroStatus = 0;  // Inativos
                else filtroStatus = 2;                     // Todos

                carregarCategorias(); // atualiza categorias conforme status
                carregar(b.search.getQuery() == null ? "" : b.search.getQuery().toString());
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Spinner Categoria
        carregarCategorias();

        b.fabAdd.setOnClickListener(v -> startActivity(new Intent(this, ProdutoFormActivity.class)));

        b.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                carregar(newText == null ? "" : newText.trim());
                return true;
            }
        });

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        TextView titulo = findViewById(R.id.txtTitulo);
        titulo.setText("Produtos");
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregar(b.search.getQuery() == null ? "" : b.search.getQuery().toString());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void carregar(String q) {
        final String termo = (q == null ? "" : q.trim());

        AppExecutors.get().io().execute(() -> {
            List<com.example.estoqueloja.data.entity.Produto> produtos;

            // 1) Lista base por status + busca
            if (filtroStatus == 1) { // ativos
                produtos = termo.isEmpty()
                        ? DbProvider.get(this).produtoDao().listarAtivos()
                        : DbProvider.get(this).produtoDao().buscarAtivos(termo);
            } else if (filtroStatus == 0) { // inativos
                produtos = termo.isEmpty()
                        ? DbProvider.get(this).produtoDao().listarInativos()
                        : DbProvider.get(this).produtoDao().buscarInativos(termo);
            } else { // todos
                produtos = termo.isEmpty()
                        ? DbProvider.get(this).produtoDao().listarTodos()
                        : DbProvider.get(this).produtoDao().buscarTodos(termo);
            }

            // 2) Aplica filtro de categoria em memória (simples e rápido)
            if (categoriaSelecionada != null && !"Todas".equalsIgnoreCase(categoriaSelecionada)) {
                java.util.List<com.example.estoqueloja.data.entity.Produto> filtrados = new java.util.ArrayList<>();
                for (com.example.estoqueloja.data.entity.Produto p : produtos) {
                    if (p.categoria != null && p.categoria.equalsIgnoreCase(categoriaSelecionada)) {
                        filtrados.add(p);
                    }
                }
                produtos = filtrados;
            }

            // 3) Estoques em lote (como você já faz)
            java.util.List<com.example.estoqueloja.data.dao.EstoqueRow> rows = DbProvider.get(this).movDao().estoquesEmLote();
            java.util.Map<Long, Integer> map = new java.util.HashMap<>();
            for (com.example.estoqueloja.data.dao.EstoqueRow r : rows) map.put(r.produtoId, r.estoque);

            final List<com.example.estoqueloja.data.entity.Produto> finalProdutos = produtos;
            AppExecutors.get().main().post(() -> adapter.setItens(finalProdutos, map));
        });
    }


    private void carregarCategorias() {
        AppExecutors.get().io().execute(() -> {
            java.util.List<String> cats;
            if (filtroStatus == 1) cats = DbProvider.get(this).produtoDao().categoriasAtivas();
            else cats = DbProvider.get(this).produtoDao().categoriasTodas();

            java.util.List<String> lista = new java.util.ArrayList<>();
            lista.add("Todas");
            lista.addAll(cats);

            AppExecutors.get().main().post(() -> {
                android.widget.ArrayAdapter<String> ad =
                        new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, lista);
                b.spCategoria.setAdapter(ad);

                // mantém seleção se existir
                int idx = lista.indexOf(categoriaSelecionada);
                if (idx >= 0) b.spCategoria.setSelection(idx);

                b.spCategoria.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                        categoriaSelecionada = (String) b.spCategoria.getSelectedItem();
                        carregar(b.search.getQuery() == null ? "" : b.search.getQuery().toString());
                    }
                    @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                });
            });
        });
    }

}
