package com.example.estoqueloja.data.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "mov_estoque",
        indices = {
                @Index(value = {"vendaId", "tipo", "obs"}),
                @Index(value = {"dataHora", "tipo"}),
                @Index(value = {"produtoId", "tipo"})
        }
)
public class MovEstoque {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long produtoId;
    public long vendaId; // 0 = antigo/sem agrupamento

    // "ENTRADA" ou "SAIDA"
    public String tipo;

    public int quantidade;     // sempre positiva
    public double custoUnit;   // em venda, salva o custo do produto no momento
    public double precoUnit;   // em venda, salva o preço de venda no momento

    public long dataHora;      // System.currentTimeMillis()
    public String obs;


}
