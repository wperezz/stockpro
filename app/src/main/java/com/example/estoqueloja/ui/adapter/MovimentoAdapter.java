package com.example.estoqueloja.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.estoqueloja.R;
import com.example.estoqueloja.data.entity.MovEstoque;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MovimentoAdapter extends RecyclerView.Adapter<MovimentoAdapter.VH> {

    private final List<MovEstoque> itens = new ArrayList<>();
    private final List<Integer> saldos = new ArrayList<>();
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());

    public void setDados(List<MovEstoque> movsAsc) {
        itens.clear();
        saldos.clear();
        itens.addAll(movsAsc);

        int saldo = 0;
        for (MovEstoque m : movsAsc) {
            if ("ENTRADA".equals(m.tipo)) saldo += m.quantidade;
            else if ("SAIDA".equals(m.tipo)) saldo -= m.quantidade;
            saldos.add(saldo);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movimento, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        MovEstoque m = itens.get(position);
        int saldo = saldos.get(position);

        String data = df.format(new Date(m.dataHora));
        h.txtLinha1.setText(m.tipo + " • " + m.quantidade + " un • " + data);

        h.txtLinha2.setText(String.format(Locale.getDefault(),
                "Preço: R$ %.2f | Custo: R$ %.2f | Saldo: %d",
                m.precoUnit, m.custoUnit, saldo));

        if (m.obs != null && !m.obs.trim().isEmpty()) {
            h.txtObs.setVisibility(View.VISIBLE);
            h.txtObs.setText(m.obs);
        } else {
            h.txtObs.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtLinha1, txtLinha2, txtObs;
        VH(@NonNull View itemView) {
            super(itemView);
            txtLinha1 = itemView.findViewById(R.id.txtLinha1);
            txtLinha2 = itemView.findViewById(R.id.txtLinha2);
            txtObs = itemView.findViewById(R.id.txtObs);
        }
    }
}
