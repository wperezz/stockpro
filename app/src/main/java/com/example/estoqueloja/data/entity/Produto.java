package com.example.estoqueloja.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "produto")
public class Produto {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String nome;

    public String categoria;
    public double custoAtual;
    public double precoVenda;
    public int estoqueMinimo;
    public int ativo = 1;

    public Produto(@NonNull String nome) {
        this.nome = nome;
    }
}
