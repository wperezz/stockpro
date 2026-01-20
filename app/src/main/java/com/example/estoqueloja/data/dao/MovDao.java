package com.example.estoqueloja.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.estoqueloja.data.entity.MovEstoque;

import java.util.List;

@Dao
public interface MovDao {

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
            "SELECT p.id as produtoId, p.nome as nome, " +
                    "SUM(m.quantidade) as qtd, " +
                    "SUM(m.precoUnit * m.quantidade) as faturamento, " +
                    "SUM((m.precoUnit - m.custoUnit) * m.quantidade) as lucro " +
                    "FROM mov_estoque m " +
                    "INNER JOIN produto p ON p.id = m.produtoId " +
                    "WHERE m.tipo='SAIDA' AND m.dataHora>=:ini AND m.dataHora<:fim " +
                    "AND (m.vendaId = 0 OR NOT EXISTS ( " +
                    "    SELECT 1 FROM mov_estoque c " +
                    "    WHERE c.vendaId = m.vendaId AND c.tipo='ENTRADA' " +
                    "      AND c.obs = ('CANCELAMENTO_VENDA#' || m.vendaId) " +
                    ")) " +
                    "GROUP BY p.id, p.nome " +
                    "ORDER BY qtd DESC " +
                    "LIMIT :limite"
    )
    List<TopVendidoRow> topVendidosValidos(long ini, long fim, int limite);

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

    @Query(
            "SELECT m.id as id, p.nome as nome, m.quantidade as quantidade, m.precoUnit as precoUnit, m.dataHora as dataHora " +
                    "FROM mov_estoque m " +
                    "JOIN produto p ON p.id = m.produtoId " +
                    "WHERE m.tipo='SAIDA' AND m.dataHora>=:ini AND m.dataHora<:fim " +
                    "AND (m.vendaId = 0 OR NOT EXISTS ( " +
                    "    SELECT 1 FROM mov_estoque c " +
                    "    WHERE c.vendaId = m.vendaId AND c.tipo='ENTRADA' " +
                    "      AND c.obs = ('CANCELAMENTO_VENDA#' || m.vendaId) " +
                    ")) " +
                    "ORDER BY m.dataHora DESC"
    )
    List<VendaDiaRow> vendasDoDiaValidas(long ini, long fim);

    @Query("SELECT COALESCE(SUM(m.precoUnit * m.quantidade),0) FROM mov_estoque m " +
            "WHERE m.tipo='SAIDA' AND m.dataHora>=:ini AND m.dataHora<:fim")
    double totalVendasPeriodo(long ini, long fim);

    @Query("SELECT COALESCE(SUM((m.precoUnit - m.custoUnit) * m.quantidade),0) FROM mov_estoque m " +
            "WHERE m.tipo='SAIDA' AND m.dataHora>=:ini AND m.dataHora<:fim")
    double lucroVendasPeriodo(long ini, long fim);

    @Query("SELECT COUNT(*) FROM mov_estoque WHERE produtoId = :produtoId")
    int countMovsDoProduto(long produtoId);

    @androidx.room.Query(
            "SELECT m.id AS movId, m.produtoId AS produtoId, m.dataHora AS dataHora, p.nome AS produtoNome, " +
                    "m.quantidade AS quantidade, m.precoUnit AS precoUnit, m.custoUnit AS custoUnit, " +
                    "(m.precoUnit * m.quantidade) AS total, " +
                    "((m.precoUnit - m.custoUnit) * m.quantidade) AS lucro " +
                    "FROM mov_estoque m " +
                    "JOIN produto p ON p.id = m.produtoId " +
                    "WHERE m.tipo = 'SAIDA' " +
                    "ORDER BY m.dataHora DESC"
    )
    java.util.List<VendaRow> listarVendas();

    // Já foi cancelada? (existe uma ENTRADA com obs = CANCELAMENTO#<id>)
    @androidx.room.Query(
            "SELECT COUNT(*) FROM mov_estoque " +
                    "WHERE tipo = 'ENTRADA' AND obs = :obsCancel"
    )
    int countCancelamentos(String obsCancel);

    @Query(
            "SELECT " +
                    "  m.vendaId AS vendaId, " +
                    "  MAX(m.dataHora) AS dataHora, " +
                    "  COUNT(*) AS itens, " +
                    "  SUM(m.quantidade) AS quantidade, " +
                    "  SUM(m.precoUnit * m.quantidade) AS total, " +
                    "  SUM((m.precoUnit - m.custoUnit) * m.quantidade) AS lucro, " +
                    "  CASE WHEN EXISTS ( " +
                    "       SELECT 1 FROM mov_estoque c " +
                    "       WHERE c.vendaId = m.vendaId AND c.tipo='ENTRADA' AND c.obs = ('CANCELAMENTO_VENDA#' || m.vendaId) " +
                    "  ) THEN 1 ELSE 0 END AS cancelada " +
                    "FROM mov_estoque m " +
                    "WHERE m.tipo='SAIDA' AND m.vendaId <> 0 " +
                    "GROUP BY m.vendaId " +
                    "ORDER BY dataHora DESC"
    )
    List<com.example.estoqueloja.data.dao.VendaResumoRow> listarVendasAgrupadas();

    @Query(
            "SELECT " +
                    "  m.id AS movId, " +
                    "  m.produtoId AS produtoId, " +
                    "  p.nome AS nome, " +
                    "  m.quantidade AS quantidade, " +
                    "  m.precoUnit AS precoUnit, " +
                    "  m.custoUnit AS custoUnit, " +
                    "  (m.precoUnit * m.quantidade) AS total, " +
                    "  ((m.precoUnit - m.custoUnit) * m.quantidade) AS lucro " +
                    "FROM mov_estoque m " +
                    "JOIN produto p ON p.id = m.produtoId " +
                    "WHERE m.vendaId = :vendaId AND m.tipo='SAIDA' " +
                    "ORDER BY m.id ASC"
    )
    List<com.example.estoqueloja.data.dao.VendaItemRow> itensDaVendaDetalhado(long vendaId);

    @Query("SELECT COUNT(*) FROM mov_estoque WHERE vendaId = :vendaId AND tipo='ENTRADA' AND obs = :obsCancel")
    int vendaJaCancelada(long vendaId, String obsCancel);

    @Query("SELECT * FROM mov_estoque WHERE vendaId = :vendaId AND tipo='SAIDA'")
    List<com.example.estoqueloja.data.entity.MovEstoque> itensDaVenda(long vendaId);

    @Insert
    void inserir(com.example.estoqueloja.data.entity.MovEstoque m);

    // filtro: 0 = válidas (default), 1 = canceladas, 2 = todas
    @Query(
            "SELECT COALESCE(SUM(m.precoUnit * m.quantidade),0) " +
                    "FROM mov_estoque m " +
                    "WHERE m.tipo='SAIDA' AND m.dataHora>=:ini AND m.dataHora<:fim " +
                    "AND ( " +
                    "   (:filtro = 2) " +
                    "   OR (:filtro = 0 AND (m.vendaId = 0 OR NOT EXISTS ( " +
                    "       SELECT 1 FROM mov_estoque c " +
                    "       WHERE c.vendaId = m.vendaId AND c.tipo='ENTRADA' " +
                    "         AND c.obs = ('CANCELAMENTO_VENDA#' || m.vendaId) " +
                    "   ))) " +
                    "   OR (:filtro = 1 AND (m.vendaId <> 0 AND EXISTS ( " +
                    "       SELECT 1 FROM mov_estoque c " +
                    "       WHERE c.vendaId = m.vendaId AND c.tipo='ENTRADA' " +
                    "         AND c.obs = ('CANCELAMENTO_VENDA#' || m.vendaId) " +
                    "   ))) " +
                    ")"
    )
    double faturamentoFiltrado(long ini, long fim, int filtro);

    @Query(
            "SELECT COALESCE(SUM((m.precoUnit - m.custoUnit) * m.quantidade),0) " +
                    "FROM mov_estoque m " +
                    "WHERE m.tipo='SAIDA' AND m.dataHora>=:ini AND m.dataHora<:fim " +
                    "AND ( " +
                    "   (:filtro = 2) " +
                    "   OR (:filtro = 0 AND (m.vendaId = 0 OR NOT EXISTS ( " +
                    "       SELECT 1 FROM mov_estoque c " +
                    "       WHERE c.vendaId = m.vendaId AND c.tipo='ENTRADA' " +
                    "         AND c.obs = ('CANCELAMENTO_VENDA#' || m.vendaId) " +
                    "   ))) " +
                    "   OR (:filtro = 1 AND (m.vendaId <> 0 AND EXISTS ( " +
                    "       SELECT 1 FROM mov_estoque c " +
                    "       WHERE c.vendaId = m.vendaId AND c.tipo='ENTRADA' " +
                    "         AND c.obs = ('CANCELAMENTO_VENDA#' || m.vendaId) " +
                    "   ))) " +
                    ")"
    )
    double lucroFiltrado(long ini, long fim, int filtro);

    @Query(
            "SELECT " +
                    "  m.vendaId AS vendaId, " +
                    "  MAX(m.dataHora) AS dataHora, " +
                    "  COUNT(*) AS itens, " +
                    "  SUM(m.quantidade) AS quantidade, " +
                    "  SUM(m.precoUnit * m.quantidade) AS total, " +
                    "  SUM((m.precoUnit - m.custoUnit) * m.quantidade) AS lucro, " +
                    "  CASE WHEN EXISTS ( " +
                    "       SELECT 1 FROM mov_estoque c " +
                    "       WHERE c.vendaId = m.vendaId AND c.tipo='ENTRADA' AND c.obs = ('CANCELAMENTO_VENDA#' || m.vendaId) " +
                    "  ) THEN 1 ELSE 0 END AS cancelada " +
                    "FROM mov_estoque m " +
                    "WHERE m.tipo='SAIDA' AND m.vendaId <> 0 " +
                    "  AND m.dataHora >= :ini " +
                    "  AND m.dataHora < :fim " +
                    "GROUP BY m.vendaId " +
                    "ORDER BY dataHora DESC"
    )
    List<VendaResumoRow> listarVendasAgrupadasPeriodo(long ini, long fim);


    @Query(
            "SELECT " +
                    "  COALESCE(SUM(precoUnit * quantidade), 0) AS total, " +
                    "  COALESCE(SUM((precoUnit - custoUnit) * quantidade), 0) AS lucro " +
                    "FROM mov_estoque " +
                    "WHERE vendaId <> 0 " +
                    "  AND tipo = 'SAIDA' " +
                    "  AND dataHora >= :ini " +
                    "  AND dataHora < :fim"
    )
    VendaTotaisRow totaisVendasPeriodo(long ini, long fim);


}
