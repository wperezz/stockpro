package com.example.estoqueloja.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "categoria",
        indices = {@Index(value = {"nome"}, unique = true)}
)
public class Categoria {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String nome;

    public Categoria(@NonNull String nome) {
        this.nome = nome;
    }
}
