package com.example.estoqueloja.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.estoqueloja.R;
import com.example.estoqueloja.data.dao.TopVendidoRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TopVendidosAdapter extends RecyclerView.Adapter<TopVendidosAdapter.VH> {

    private final List<TopVendidoRow> itens = new ArrayList<>();

    public void setItens(List<TopVendidoRow> rows) {
        itens.clear();
        itens.addAll(rows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_top_vendido, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        TopVendidoRow r = itens.get(position);
        h.txtNome.setText((position + 1) + ". " + r.nome);
        h.txtDados.setText(String.format(Locale.getDefault(),
                "Qtd: %d | Fat: R$ %.2f | Lucro: R$ %.2f",
                r.qtd, r.faturamento, r.lucro));
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtNome, txtDados;
        VH(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtNome);
            txtDados = itemView.findViewById(R.id.txtDados);
        }
    }
}
