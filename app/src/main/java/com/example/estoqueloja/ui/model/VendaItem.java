package com.example.estoqueloja.ui.model;

import com.example.estoqueloja.data.entity.Produto;

public class VendaItem {
    public Produto produto;
    public int qtd;

    public VendaItem(Produto produto, int qtd) {
        this.produto = produto;
        this.qtd = qtd;
    }

    public double total() {
        return produto.precoVenda * qtd;
    }

    public double lucro() {
        return (produto.precoVenda - produto.custoAtual) * qtd;
    }
}
