package com.example.estoqueloja.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.estoqueloja.R;
import com.example.estoqueloja.data.db.DbProvider;
import com.example.estoqueloja.data.entity.MovEstoque;
import com.example.estoqueloja.databinding.ActivityVendaDetalheBinding;
import com.example.estoqueloja.ui.adapter.VendaDetalheAdapter;
import com.example.estoqueloja.util.AppExecutors;
import com.example.estoqueloja.util.InsetsUtil;

import java.util.List;
import java.util.Locale;

public class VendaDetalheActivity extends AppCompatActivity {

    private ActivityVendaDetalheBinding b;
    private VendaDetalheAdapter adapter;
    private long vendaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityVendaDetalheBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        InsetsUtil.applyPaddingSystemBarsTopBottom(b.getRoot());

        TextView titulo = findViewById(R.id.txtTitulo);
        titulo.setText("Detalhe da Venda");

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        vendaId = getIntent().getLongExtra("vendaId", 0);
        if (vendaId == 0) {
            Toast.makeText(this, "Venda inválida.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        adapter = new VendaDetalheAdapter();
        b.recyclerItens.setLayoutManager(new LinearLayoutManager(this));
        b.recyclerItens.setAdapter(adapter);

        b.btnCancelarVenda.setOnClickListener(v -> confirmarCancelamento());
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
            List<com.example.estoqueloja.data.dao.VendaItemRow> itens =
                    DbProvider.get(this).movDao().itensDaVendaDetalhado(vendaId);

            double total = 0, lucro = 0;
            int qtd = 0;
            for (com.example.estoqueloja.data.dao.VendaItemRow r : itens) {
                total += r.total;
                lucro += r.lucro;
                qtd += r.quantidade;
            }

            String obsCancel = "CANCELAMENTO_VENDA#" + vendaId;
            int jaCancelada = DbProvider.get(this).movDao().vendaJaCancelada(vendaId, obsCancel);

            double fTotal = total;
            double fLucro = lucro;
            int fQtd = qtd;
            boolean cancelada = (jaCancelada > 0);

            AppExecutors.get().main().post(() -> {
                b.txtResumoVenda.setText(String.format(Locale.getDefault(),
                        "Venda #%d | Qtd: %d | Total: R$ %.2f | Lucro: R$ %.2f",
                        vendaId, fQtd, fTotal, fLucro));

                adapter.setItens(itens);

                b.btnCancelarVenda.setEnabled(!cancelada);
                if (cancelada) b.btnCancelarVenda.setText("Venda já cancelada");
                else b.btnCancelarVenda.setText("Cancelar / Estornar venda");
            });
        });
    }

    private void confirmarCancelamento() {
        new AlertDialog.Builder(this)
                .setTitle("Cancelar venda")
                .setMessage("Isso irá estornar todos os itens (entrada no estoque) e manter o histórico. Confirmar?")
                .setPositiveButton("Sim, cancelar", (d, w) -> cancelarVenda())
                .setNegativeButton("Não", null)
                .show();
    }

    private void cancelarVenda() {
        AppExecutors.get().io().execute(() -> {
            String obsCancel = "CANCELAMENTO_VENDA#" + vendaId;

            int ja = DbProvider.get(this).movDao().vendaJaCancelada(vendaId, obsCancel);
            if (ja > 0) {
                AppExecutors.get().main().post(() ->
                        Toast.makeText(this, "Esta venda já foi cancelada.", Toast.LENGTH_LONG).show());
                return;
            }

            List<MovEstoque> saidas = DbProvider.get(this).movDao().itensDaVenda(vendaId);
            if (saidas == null || saidas.isEmpty()) {
                AppExecutors.get().main().post(() ->
                        Toast.makeText(this, "Itens da venda não encontrados.", Toast.LENGTH_LONG).show());
                return;
            }

            long agora = System.currentTimeMillis();

            for (MovEstoque s : saidas) {
                MovEstoque e = new MovEstoque();
                e.vendaId = vendaId;
                e.produtoId = s.produtoId;
                e.tipo = "ENTRADA";
                e.quantidade = s.quantidade;
                e.dataHora = agora;
                e.obs = obsCancel;
                e.custoUnit = s.custoUnit;
                e.precoUnit = s.precoUnit;
                DbProvider.get(this).movDao().inserir(e);
            }

            AppExecutors.get().main().post(() -> {
                Toast.makeText(this, "Venda cancelada e estornada!", Toast.LENGTH_LONG).show();
                carregar();
            });
        });
    }
}
