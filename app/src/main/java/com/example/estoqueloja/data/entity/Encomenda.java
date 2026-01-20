package com.example.estoqueloja.data.entity;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Entity(
        tableName = "encomenda",
        indices = {@Index(value = {"dataHora"}), @Index(value = {"status"})}
)
public class Encomenda {

    public static final int PENDENTE = 0;
    public static final int ENTREGUE = 1;
    public static final int CANCELADO = 2;

    @IntDef({PENDENTE, ENTREGUE, CANCELADO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Status {}

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull public String nomePessoa;
    @NonNull public String descricao;
    public int quantidade;
    public long dataHora;

    @Status public int status = PENDENTE;

    public Encomenda(@NonNull String nomePessoa, @NonNull String descricao, int quantidade) {
        this.nomePessoa = nomePessoa;
        this.descricao = descricao;
        this.quantidade = quantidade;
        this.dataHora = System.currentTimeMillis();
        this.status = PENDENTE;
    }
}
