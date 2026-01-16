package com.example.estoqueloja.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.estoqueloja.R;
import com.example.estoqueloja.data.entity.Produto;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProdutoAdapter extends RecyclerView.Adapter<ProdutoAdapter.VH> {

    public interface OnClickProduto {
        void onClick(Produto p);
    }

    public interface OnLongClickProduto {
        void onLongClick(Produto p);
    }

    private final List<Produto> itens = new ArrayList<>();
    private final Map<Long, Integer> estoqueMap = new HashMap<>();
    private final OnClickProduto onClick;
    private final OnLongClickProduto onLongClick;

    public ProdutoAdapter(OnClickProduto onClick, OnLongClickProduto onLongClick) {
        this.onClick = onClick;
        this.onLongClick = onLongClick;
    }

    public void setItens(List<Produto> novos, Map<Long, Integer> novoMap) {
        itens.clear();
        if (novos != null) itens.addAll(novos);

        estoqueMap.clear();
        if (novoMap != null) estoqueMap.putAll(novoMap);

        notifyDataSetChanged();
    }

    private int estoqueDe(long produtoId) {
        Integer v = estoqueMap.get(produtoId);
        return v == null ? 0 : v;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_produto, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Produto p = itens.get(position);

        int estoque = estoqueDe(p.id);
        boolean baixo = estoque <= p.estoqueMinimo;
        boolean inativo = (p.ativo == 0);

        // Nome + status
        h.txtNome.setText(inativo ? (p.nome + " (Inativo)") : p.nome);

        // Demais infos
        h.txtCategoria.setText(p.categoria == null ? "" : p.categoria);

        h.txtPreco.setText(String.format(Locale.getDefault(),
                "Venda: R$ %.2f | Custo: R$ %.2f", p.precoVenda, p.custoAtual));

        h.txtEstoque.setText("Estoque: " + estoque + " | Mín: " + p.estoqueMinimo);

        // Alerta (estoque baixo)
        h.txtAlerta.setVisibility((baixo && !inativo) ? View.VISIBLE : View.GONE);

        // Estilo do item (inativo fica apagado)
        h.itemView.setAlpha(inativo ? 0.45f : 1.0f);

        // Card: borda/elevação conforme estoque baixo
        if (h.cardRoot != null) {
            if (baixo) {
                h.cardRoot.setStrokeWidth(2);
                h.cardRoot.setStrokeColor(0xFFB00020); // vermelho
                h.cardRoot.setCardElevation(6f);
            } else {
                h.cardRoot.setStrokeWidth(1);
                h.cardRoot.setStrokeColor(0x22000000); // cinza leve
                h.cardRoot.setCardElevation(2f);
            }
        }

        // Clique normal
        h.itemView.setOnClickListener(v -> {
            if (onClick != null) onClick.onClick(p);
        });

        // Clique longo
        h.itemView.setOnLongClickListener(v -> {
            if (onLongClick != null) onLongClick.onLongClick(p);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtNome, txtCategoria, txtPreco, txtEstoque, txtAlerta;
        MaterialCardView cardRoot;

        VH(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtNome);
            txtCategoria = itemView.findViewById(R.id.txtCategoria);
            txtPreco = itemView.findViewById(R.id.txtPreco);
            txtEstoque = itemView.findViewById(R.id.txtEstoque);
            txtAlerta = itemView.findViewById(R.id.txtAlerta);
            cardRoot = itemView.findViewById(R.id.cardRoot);
        }
    }
}
