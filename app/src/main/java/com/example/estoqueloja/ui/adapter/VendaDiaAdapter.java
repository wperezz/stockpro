package com.example.estoqueloja.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.estoqueloja.R;
import com.example.estoqueloja.data.dao.VendaDiaRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VendaDiaAdapter extends RecyclerView.Adapter<VendaDiaAdapter.VH> {

    private final List<VendaDiaRow> itens = new ArrayList<>();

    public void setItens(List<VendaDiaRow> lista) {
        itens.clear();
        if (lista != null) itens.addAll(lista);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_venda_dia, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        VendaDiaRow r = itens.get(position);
        h.txtNome.setText(r.nome);
        h.txtInfo.setText(String.format(Locale.getDefault(),
                "Qtd: %d | Unit: R$ %.2f | Total: R$ %.2f",
                r.quantidade, r.precoUnit, (r.precoUnit * r.quantidade)));
    }

    @Override
    public int getItemCount() { return itens.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtNome, txtInfo;
        VH(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtNome);
            txtInfo = itemView.findViewById(R.id.txtInfo);
        }
    }
}
