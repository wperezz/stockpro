package com.example.estoqueloja.data.dao;

public class VendaRow {
    public long movId;
    public long produtoId; // <-- adicionar
    public long dataHora;
    public String produtoNome;
    public int quantidade;
    public double precoUnit;
    public double custoUnit;
    public double total;
    public double lucro;
}
