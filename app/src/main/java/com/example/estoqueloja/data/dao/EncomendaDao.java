package com.example.estoqueloja.data.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Insert;

import com.example.estoqueloja.data.entity.Encomenda;

import java.util.List;

@Dao
public interface EncomendaDao {

    @Query("SELECT * FROM encomenda ORDER BY dataHora DESC")
    List<Encomenda> listar();

    @Insert
    long inserir(Encomenda e);

    @Query("UPDATE encomenda SET status = :status WHERE id = :id")
    int atualizarStatus(long id, int status);

    @Query("DELETE FROM encomenda WHERE id = :id")
    int deletarPorId(long id);
}

