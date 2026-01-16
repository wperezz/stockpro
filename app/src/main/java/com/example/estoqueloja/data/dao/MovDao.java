package com.example.estoqueloja.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.estoqueloja.data.entity.MovEstoque;

import java.util.List;

@Dao
public interface MovDao {

    @Insert
    long inserir(MovEstoque m);

    @Query("SELECT * FROM mov_estoque WHERE produtoId=:produtoId ORDER BY dataHora DESC")
    List<MovEstoque> listarPorProduto(long produtoId);

    @Query(
            "SELECT " +
                    "COALESCE(SUM(CASE WHEN tipo='ENTRADA' THEN quantidade ELSE 0 END),0) " +
                    "- COALESCE(SUM(CASE WHEN tipo='SAIDA' THEN quantidade ELSE 0 END),0) " +
                    "FROM mov_estoque WHERE produtoId=:produtoId"
    )
    int estoqueAtual(long produtoId);

    @Query("SELECT COALESCE(SUM(precoUnit * quantidade),0) FROM mov_estoque " +
            "WHERE tipo='SAIDA' AND dataHora>=:ini AND dataHora<:fim")
    double faturamento(long ini, long fim);

    @Query("SELECT COALESCE(SUM((precoUnit - custoUnit) * quantidade),0) FROM mov_estoque " +
            "WHERE tipo='SAIDA' AND dataHora>=:ini AND dataHora<:fim")
    double lucro(long ini, long fim);

    @Query("SELECT * FROM mov_estoque WHERE produtoId=:produtoId ORDER BY dataHora ASC")
    List<MovEstoque> listarPorProdutoAsc(long produtoId);

    @Query("SELECT * FROM mov_estoque WHERE produtoId=:produtoId ORDER BY dataHora DESC")
    List<MovEstoque> listarPorProdutoDesc(long produtoId);

    @Query("SELECT p.id as produtoId, p.nome as nome, " +
            "SUM(m.quantidade) as qtd, " +
            "SUM(m.precoUnit * m.quantidade) as faturamento, " +
            "SUM((m.precoUnit - m.custoUnit) * m.quantidade) as lucro " +
            "FROM mov_estoque m " +
            "INNER JOIN produto p ON p.id = m.produtoId " +
            "WHERE m.tipo='SAIDA' AND m.dataHora>=:ini AND m.dataHora<:fim " +
            "GROUP BY p.id, p.nome " +
            "ORDER BY qtd DESC " +
            "LIMIT :limite")
    java.util.List<com.example.estoqueloja.data.dao.TopVendidoRow> topVendidos(long ini, long fim, int limite);

    @Query(
            "SELECT produtoId as produtoId, " +
                    "COALESCE(SUM(CASE WHEN tipo='ENTRADA' THEN quantidade ELSE 0 END),0) " +
                    "- COALESCE(SUM(CASE WHEN tipo='SAIDA' THEN quantidade ELSE 0 END),0) " +
                    "as estoque " +
                    "FROM mov_estoque " +
                    "GROUP BY produtoId"
    )
    java.util.List<com.example.estoqueloja.data.dao.EstoqueRow> estoquesEmLote();

    @Query("SELECT m.id as id, p.nome as nome, m.quantidade as quantidade, m.precoUnit as precoUnit, m.dataHora as dataHora " +
            "FROM mov_estoque m " +
            "JOIN produto p ON p.id = m.produtoId " +
            "WHERE m.tipo='SAIDA' AND m.dataHora>=:ini AND m.dataHora<:fim " +
            "ORDER BY m.dataHora DESC")
    java.util.List<com.example.estoqueloja.data.dao.VendaDiaRow> vendasDoDia(long ini, long fim);

    @Query("SELECT COALESCE(SUM(m.precoUnit * m.quantidade),0) FROM mov_estoque m " +
            "WHERE m.tipo='SAIDA' AND m.dataHora>=:ini AND m.dataHora<:fim")
    double totalVendasPeriodo(long ini, long fim);

    @Query("SELECT COALESCE(SUM((m.precoUnit - m.custoUnit) * m.quantidade),0) FROM mov_estoque m " +
            "WHERE m.tipo='SAIDA' AND m.dataHora>=:ini AND m.dataHora<:fim")
    double lucroVendasPeriodo(long ini, long fim);

    @Query("SELECT COUNT(*) FROM mov_estoque WHERE produtoId = :produtoId")
    int countMovsDoProduto(long produtoId);


}
