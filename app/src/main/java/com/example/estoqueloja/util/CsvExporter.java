package com.example.estoqueloja.util;

import android.content.Context;
import android.net.Uri;

import com.example.estoqueloja.data.db.DbProvider;
import com.example.estoqueloja.data.entity.MovEstoque;
import com.example.estoqueloja.data.entity.Produto;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CsvExporter {

    public static void exportarTudo(Context ctx, Uri uri) throws Exception {
        List<Produto> produtos = DbProvider.get(ctx).produtoDao().listarAtivos();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        try (OutputStream os = ctx.getContentResolver().openOutputStream(uri)) {
            if (os == null) throw new Exception("Não foi possível abrir o arquivo.");

            StringBuilder sb = new StringBuilder();

            sb.append("PRODUTOS\n");
            sb.append("id;nome;categoria;custoAtual;precoVenda;estoqueMinimo;local;estoqueAtual\n");

            for (Produto p : produtos) {
                int estoque = DbProvider.get(ctx).movDao().estoqueAtual(p.id);
                sb.append(p.id).append(";")
                        .append(esc(p.nome)).append(";")
                        .append(esc(nvl(p.categoria))).append(";")
                        .append(p.custoAtual).append(";")
                        .append(p.precoVenda).append(";")
                        .append(p.estoqueMinimo).append(";")
                        .append(estoque).append("\n");
            }

            sb.append("\nMOVIMENTACOES\n");
            sb.append("id;produtoId;tipo;quantidade;custoUnit;precoUnit;dataHora;obs\n");

            for (Produto p : produtos) {
                List<MovEstoque> movs = DbProvider.get(ctx).movDao().listarPorProdutoDesc(p.id);
                for (MovEstoque m : movs) {
                    sb.append(m.id).append(";")
                            .append(m.produtoId).append(";")
                            .append(m.tipo).append(";")
                            .append(m.quantidade).append(";")
                            .append(m.custoUnit).append(";")
                            .append(m.precoUnit).append(";")
                            .append(df.format(new Date(m.dataHora))).append(";")
                            .append(esc(nvl(m.obs))).append("\n");
                }
            }

            os.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
    }

    private static String nvl(String s) { return s == null ? "" : s; }

    private static String esc(String s) {
        // CSV simples com ; — remove quebras de linha
        return s.replace("\n", " ").replace("\r", " ");
    }
}