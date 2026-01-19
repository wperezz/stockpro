package com.example.estoqueloja.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.estoqueloja.R;
import com.example.estoqueloja.data.dao.VendaItemRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VendaDetalheAdapter extends RecyclerView.Adapter<VendaDetalheAdapter.VH> {

    private final List<VendaItemRow> itens = new ArrayList<>();

    public void setItens(List<VendaItemRow> lista) {
        itens.clear();
        if (lista != null) itens.addAll(lista);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_venda_detalhe, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        VendaItemRow r = itens.get(position);

        h.txtNome.setText(r.nome);
        h.txtQtd.setText("Qtd: " + r.quantidade);
        h.txtValores.setText(String.format(Locale.getDefault(),
                "Total: R$ %.2f | Lucro: R$ %.2f", r.total, r.lucro));
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtNome, txtQtd, txtValores;

        VH(@NonNull View v) {
            super(v);
            txtNome = v.findViewById(R.id.txtNome);
            txtQtd = v.findViewById(R.id.txtQtd);
            txtValores = v.findViewById(R.id.txtValores);
        }
    }
}
