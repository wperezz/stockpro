package com.example.estoqueloja.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.estoqueloja.R;
import com.example.estoqueloja.data.entity.Categoria;

import java.util.List;

public class CategoriasAdapter extends RecyclerView.Adapter<CategoriasAdapter.VH> {

    public interface Callback {
        void onExcluir(Categoria c);
    }

    private final List<Categoria> itens;
    private final Callback cb;

    public CategoriasAdapter(List<Categoria> itens, Callback cb) {
        this.itens = itens;
        this.cb = cb;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_categoria, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Categoria c = itens.get(position);
        h.txtNome.setText(c.nome);

        h.btnExcluir.setOnClickListener(v -> {
            if (cb != null) cb.onExcluir(c);
        });
    }

    @Override
    public int getItemCount() {
        return itens == null ? 0 : itens.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtNome;
        ImageButton btnExcluir;

        VH(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtNome);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);
        }
    }
}
