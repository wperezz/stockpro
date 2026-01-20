package com.example.estoqueloja.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.estoqueloja.data.entity.Categoria;

import java.util.List;

@Dao
public interface CategoriaDao {

    @Query("SELECT * FROM categoria ORDER BY nome")
    List<Categoria> listar();

    @Insert
    long inserir(Categoria c);

    @Delete
    int deletar(Categoria c);

    @Query("DELETE FROM categoria WHERE id = :id")
    int deletarPorId(long id);

    @Query("SELECT COUNT(*) FROM categoria WHERE LOWER(TRIM(nome)) = LOWER(TRIM(:nome))")
    int existeComNome(String nome);
}

