package com.example.estoqueloja.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.estoqueloja.R;
import com.example.estoqueloja.data.entity.Encomenda;

import java.util.List;

import com.google.android.material.chip.ChipGroup;

public class EncomendasAdapter extends RecyclerView.Adapter<EncomendasAdapter.VH> {

    public interface Callback {
        void onExcluir(Encomenda e);
        void onStatusChanged(Encomenda e, int novoStatus);
    }

    private final List<Encomenda> itens;
    private final Callback cb;

    public EncomendasAdapter(List<Encomenda> itens, Callback cb) {
        this.itens = itens;
        this.cb = cb;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_encomenda, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Encomenda e = itens.get(position);

        h.txtPessoa.setText(e.nomePessoa);
        h.txtDescricao.setText(e.descricao);
        h.txtQtd.setText("Qtd: " + e.quantidade);

        // evita disparar listener ao setar check programaticamente
        h.cgStatus.setOnCheckedChangeListener(null);

        if (e.status == Encomenda.PENDENTE) h.cgStatus.check(R.id.chPendente);
        else if (e.status == Encomenda.ENTREGUE) h.cgStatus.check(R.id.chEntregue);
        else h.cgStatus.check(R.id.chCancelado);

        h.cgStatus.setOnCheckedChangeListener((group, checkedId) -> {
            int novo;
            if (checkedId == R.id.chEntregue) novo = Encomenda.ENTREGUE;
            else if (checkedId == R.id.chCancelado) novo = Encomenda.CANCELADO;
            else novo = Encomenda.PENDENTE;

            if (novo != e.status && cb != null) cb.onStatusChanged(e, novo);
        });

        h.btnExcluir.setOnClickListener(v -> {
            if (cb != null) cb.onExcluir(e);
        });
    }

    @Override
    public int getItemCount() {
        return itens == null ? 0 : itens.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtPessoa, txtDescricao, txtQtd;
        ImageButton btnExcluir;
        ChipGroup cgStatus;

        VH(@NonNull View itemView) {
            super(itemView);
            txtPessoa = itemView.findViewById(R.id.txtPessoa);
            txtDescricao = itemView.findViewById(R.id.txtDescricao);
            txtQtd = itemView.findViewById(R.id.txtQtd);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);
            cgStatus = itemView.findViewById(R.id.cgStatus);
        }
    }
}

