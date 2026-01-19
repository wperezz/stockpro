package com.example.estoqueloja.data.dao;

public class VendaResumoRow {
    public long vendaId;
    public long dataHora;      // última data/hora do grupo
    public int itens;          // qtd de linhas (itens)
    public int quantidade;     // soma quantidades
    public double total;       // soma total
    public double lucro;       // soma lucro
    public int cancelada;      // 1 ou 0
}