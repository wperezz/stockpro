package com.example.estoqueloja.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.estoqueloja.R;
import com.example.estoqueloja.ui.model.VendaItem;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VendaCarrinhoAdapter extends RecyclerView.Adapter<VendaCarrinhoAdapter.VH> {

    public interface Callback {
        void onMais(int pos);
        void onMenos(int pos);
        void onRemover(int pos);
        void onEditarQtd(int pos);
    }

    private final List<VendaItem> itens = new ArrayList<>();
    private final Callback cb;

    public VendaCarrinhoAdapter(Callback cb) {
        this.cb = cb;
    }

    public void setItens(List<VendaItem> lista) {
        itens.clear();
        if (lista != null) itens.addAll(lista);
        notifyDataSetChanged();
    }

    public VendaItem getItem(int pos) {
        return itens.get(pos);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_venda_carrinho, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        VendaItem it = itens.get(position);

        h.txtNome.setText(it.produto.nome);
        h.txtQtd.setText(String.valueOf(it.qtd));

        double total = it.total();
        h.txtResumo.setText(String.format(Locale.getDefault(),
                "Preço: R$ %.2f | Total: R$ %.2f",
                it.produto.precoVenda, total));

        h.btnMais.setOnClickListener(v -> { if (cb != null) cb.onMais(position); });
        h.btnMenos.setOnClickListener(v -> { if (cb != null) cb.onMenos(position); });
        h.btnRemover.setOnClickListener(v -> { if (cb != null) cb.onRemover(position); });
        h.txtQtd.setOnClickListener(v -> { if (cb != null) cb.onEditarQtd(position); });
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtNome, txtResumo, txtQtd;
        MaterialButton btnMais, btnMenos, btnRemover;

        VH(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtNome);
            txtResumo = itemView.findViewById(R.id.txtResumo);
            txtQtd = itemView.findViewById(R.id.txtQtd);
            btnMais = itemView.findViewById(R.id.btnMais);
            btnMenos = itemView.findViewById(R.id.btnMenos);
            btnRemover = itemView.findViewById(R.id.btnRemover);
        }
    }
}
