package com.example.estoqueloja.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.estoqueloja.R;
import com.example.estoqueloja.data.dao.VendaResumoRow;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VendasAdapter extends RecyclerView.Adapter<VendasAdapter.VH> {

    public interface OnClick {
        void onClick(VendaResumoRow v);
    }

    private final List<VendaResumoRow> itens = new ArrayList<>();
    private final OnClick onClick;

    public VendasAdapter(OnClick onClick) {
        this.onClick = onClick;
    }

    public void setItens(List<VendaResumoRow> lista) {
        itens.clear();
        if (lista != null) itens.addAll(lista);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_venda, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        VendaResumoRow r = itens.get(position);

        SimpleDateFormat df = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());

        h.txtVendaId.setText("Venda #" + r.vendaId);
        h.txtData.setText(df.format(new Date(r.dataHora)));
        h.txtResumo.setText("Itens: " + r.itens + " | Qtd: " + r.quantidade);
        h.txtValores.setText(String.format(Locale.getDefault(),
                "Total: R$ %.2f | Lucro: R$ %.2f", r.total, r.lucro));

        boolean cancelada = (r.cancelada == 1);
        h.txtCancelada.setVisibility(cancelada ? View.VISIBLE : View.GONE);
        h.itemView.setAlpha(cancelada ? 0.55f : 1.0f);

        h.itemView.setOnClickListener(v -> {
            if (onClick != null) onClick.onClick(r);
        });
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtVendaId, txtData, txtResumo, txtValores, txtCancelada;

        VH(@NonNull View v) {
            super(v);
            txtVendaId = v.findViewById(R.id.txtVendaId);
            txtData = v.findViewById(R.id.txtData);
            txtResumo = v.findViewById(R.id.txtResumo);
            txtValores = v.findViewById(R.id.txtValores);
            txtCancelada = v.findViewById(R.id.txtCancelada);
        }
    }
}
