package com.example.estoqueloja.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.estoqueloja.R;
import com.example.estoqueloja.data.db.DbProvider;
import com.example.estoqueloja.data.entity.Encomenda;
import com.example.estoqueloja.databinding.ActivityEncomendasBinding;
import com.example.estoqueloja.ui.adapter.EncomendasAdapter;
import com.example.estoqueloja.util.AppExecutors;
import com.example.estoqueloja.util.InsetsUtil;

import java.util.ArrayList;
import java.util.List;

public class EncomendasActivity extends AppCompatActivity {

    private ActivityEncomendasBinding b;

    private final List<Encomenda> lista = new ArrayList<>();
    private EncomendasAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityEncomendasBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        ((TextView)findViewById(R.id.txtTitulo)).setText("Encomendas");

        b.recyclerEncomendas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EncomendasAdapter(lista, new EncomendasAdapter.Callback() {
            @Override public void onExcluir(Encomenda e) { confirmarExcluir(e); }

            @Override public void onStatusChanged(Encomenda e, int novoStatus) {
                atualizarStatus(e, novoStatus);
            }
        });
        b.recyclerEncomendas.setAdapter(adapter);

        b.btnSalvarEncomenda.setOnClickListener(v -> salvar());
        InsetsUtil.applyPaddingSystemBarsTopBottom(findViewById(R.id.rootEncomendas));
        carregar();
    }

    private void atualizarStatus(Encomenda e, int novoStatus) {
        AppExecutors.get().io().execute(() -> {
            DbProvider.get(this).encomendaDao().atualizarStatus(e.id, novoStatus);
            e.status = novoStatus; // atualiza objeto em memória
            AppExecutors.get().main().post(() -> adapter.notifyDataSetChanged());
        });
    }


    private void carregar() {
        AppExecutors.get().io().execute(() -> {
            List<Encomenda> itens = DbProvider.get(this).encomendaDao().listar();
            AppExecutors.get().main().post(() -> {
                lista.clear();
                lista.addAll(itens);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void salvar() {
        String pessoa = b.edNomePessoa.getText() == null ? "" : b.edNomePessoa.getText().toString().trim();
        String desc = b.edDescricao.getText() == null ? "" : b.edDescricao.getText().toString().trim();
        int qtd = parseInt(b.edQuantidade.getText() == null ? "" : b.edQuantidade.getText().toString());

        if (pessoa.isEmpty()) { b.edNomePessoa.setError("Informe o nome"); return; }
        if (desc.isEmpty()) { b.edDescricao.setError("Informe a encomenda"); return; }
        if (qtd <= 0) { b.edQuantidade.setError("Informe a quantidade"); return; }

        Encomenda e = new Encomenda(pessoa, desc, qtd);

        AppExecutors.get().io().execute(() -> {
            DbProvider.get(this).encomendaDao().inserir(e);
            AppExecutors.get().main().post(() -> {
                Toast.makeText(this, "Encomenda salva!", Toast.LENGTH_SHORT).show();
                b.edDescricao.setText("");
                b.edQuantidade.setText("");
                carregar();
            });
        });
    }

    private void confirmarExcluir(Encomenda e) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir encomenda")
                .setMessage("Deseja excluir a encomenda de \"" + e.nomePessoa + "\"?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (d,w) -> excluir(e))
                .show();
    }

    private void excluir(Encomenda e) {
        AppExecutors.get().io().execute(() -> {
            DbProvider.get(this).encomendaDao().deletarPorId(e.id);
            AppExecutors.get().main().post(this::carregar);
        });
    }

    private int parseInt(String s) {
        if (s == null) return 0;
        s = s.trim();
        if (s.isEmpty()) return 0;
        try { return Integer.parseInt(s); }
        catch (Exception ex) { return 0; }
    }
}

