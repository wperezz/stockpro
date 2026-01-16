package com.example.estoqueloja.data.dao;

import androidx.room.*;
import com.example.estoqueloja.data.entity.Produto;
import java.util.List;

@Dao
public interface ProdutoDao {

    @Insert
    long inserir(Produto p);

    @Update
    void atualizar(Produto p);

    @Delete
    void deletar(Produto p);

    @Query("SELECT * FROM produto WHERE ativo = 1 ORDER BY nome")
    List<Produto> listarAtivos();

    @Query("SELECT * FROM produto WHERE ativo = 0 ORDER BY nome")
    List<Produto> listarInativos();

    @Query("SELECT * FROM produto ORDER BY nome")
    List<Produto> listarTodos();

    @Query("SELECT * FROM produto WHERE ativo = 1 AND nome LIKE '%' || :q || '%' ORDER BY nome")
    List<Produto> buscarAtivos(String q);

    @Query("SELECT * FROM produto WHERE ativo = 0 AND nome LIKE '%' || :q || '%' ORDER BY nome")
    List<Produto> buscarInativos(String q);

    @Query("SELECT * FROM produto WHERE nome LIKE '%' || :q || '%' ORDER BY nome")
    List<Produto> buscarTodos(String q);


    @Query("SELECT * FROM produto WHERE id = :id LIMIT 1")
    Produto obter(long id);

    @Query("SELECT DISTINCT categoria FROM produto WHERE categoria IS NOT NULL AND TRIM(categoria) <> '' ORDER BY categoria")
    java.util.List<String> categorias();

    @Query("SELECT * FROM produto WHERE categoria = :cat ORDER BY nome")
    java.util.List<com.example.estoqueloja.data.entity.Produto> listarPorCategoria(String cat);

    @Query("SELECT * FROM produto WHERE categoria = :cat AND (nome LIKE '%' || :q || '%') ORDER BY nome")
    java.util.List<com.example.estoqueloja.data.entity.Produto> buscarPorCategoria(String cat, String q);

    // 🔹 Ativar / Inativar
    @Query("UPDATE produto SET ativo = :ativo WHERE id = :id")
    void setAtivo(long id, int ativo);

    @Query(
            "SELECT DISTINCT categoria " +
                    "FROM produto " +
                    "WHERE categoria IS NOT NULL AND TRIM(categoria) <> '' " +
                    "ORDER BY categoria"
    )
    List<String> categoriasTodas();

    @Query(
            "SELECT DISTINCT categoria " +
                    "FROM produto " +
                    "WHERE ativo = 1 " +
                    "AND categoria IS NOT NULL " +
                    "AND TRIM(categoria) <> '' " +
                    "ORDER BY categoria"
    )
    List<String> categoriasAtivas();

}