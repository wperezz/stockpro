package com.example.estoqueloja.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.estoqueloja.R;
import com.example.estoqueloja.data.db.DbProvider;
import com.example.estoqueloja.data.entity.Categoria;
import com.example.estoqueloja.databinding.ActivityCategoriasBinding;
import com.example.estoqueloja.ui.adapter.CategoriasAdapter;
import com.example.estoqueloja.util.AppExecutors;

import java.util.ArrayList;
import java.util.List;

public class CategoriasActivity extends AppCompatActivity {

    private ActivityCategoriasBinding b;
    private final List<Categoria> lista = new ArrayList<>();
    private CategoriasAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityCategoriasBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        ((TextView)findViewById(R.id.txtTitulo)).setText("Categorias");

        b.recyclerCategorias.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoriasAdapter(lista, cat -> excluirCategoria(cat));
        b.recyclerCategorias.setAdapter(adapter);

        b.btnAddCategoria.setOnClickListener(v -> adicionarCategoria());

        carregar();
    }

    private void carregar() {
        AppExecutors.get().io().execute(() -> {
            List<Categoria> cats = DbProvider.get(this).categoriaDao().listar();
            AppExecutors.get().main().post(() -> {
                lista.clear();
                lista.addAll(cats);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void adicionarCategoria() {
        String nome = b.edNomeCategoria.getText() == null ? "" : b.edNomeCategoria.getText().toString().trim();
        if (nome.isEmpty()) {
            b.edNomeCategoria.setError("Informe o nome");
            return;
        }

        AppExecutors.get().io().execute(() -> {
            int existe = DbProvider.get(this).categoriaDao().existeComNome(nome);
            if (existe > 0) {
                AppExecutors.get().main().post(() ->
                        Toast.makeText(this, "Categoria já existe.", Toast.LENGTH_SHORT).show());
                return;
            }

            DbProvider.get(this).categoriaDao().inserir(new Categoria(nome));
            AppExecutors.get().main().post(() -> {
                b.edNomeCategoria.setText("");
                carregar();
                setResult(Activity.RESULT_OK); // avisa quem chamou pra atualizar dropdown
            });
        });
    }

    private void excluirCategoria(Categoria c) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir categoria")
                .setMessage("Deseja excluir \"" + c.nome + "\"?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (d,w) -> {
                    AppExecutors.get().io().execute(() -> {
                        DbProvider.get(this).categoriaDao().deletarPorId(c.id);
                        AppExecutors.get().main().post(() -> {
                            carregar();
                            setResult(Activity.RESULT_OK);
                        });
                    });
                })
                .show();
    }
}
