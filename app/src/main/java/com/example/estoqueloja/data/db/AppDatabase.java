package com.example.estoqueloja.data.db;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.estoqueloja.data.dao.CategoriaDao;
import com.example.estoqueloja.data.dao.EncomendaDao;
import com.example.estoqueloja.data.dao.MovDao;
import com.example.estoqueloja.data.dao.ProdutoDao;
import com.example.estoqueloja.data.entity.Categoria;
import com.example.estoqueloja.data.entity.Encomenda;
import com.example.estoqueloja.data.entity.MovEstoque;
import com.example.estoqueloja.data.entity.Produto;

@Database(entities = {Produto.class, MovEstoque.class, Categoria.class, Encomenda.class}, version = 6)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ProdutoDao produtoDao();
    public abstract MovDao movDao();
    public abstract CategoriaDao categoriaDao();
    public abstract EncomendaDao encomendaDao();

    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE mov_estoque ADD COLUMN vendaId INTEGER NOT NULL DEFAULT 0");
        }
    };

    // (Opcional) Se quiser preservar dados entre 4 e 5:
    public static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS categoria (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "nome TEXT NOT NULL" +
                            ")"
            );
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_categoria_nome ON categoria(nome)");
        }
    };

    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS encomenda (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "nomePessoa TEXT NOT NULL, " +
                            "descricao TEXT NOT NULL, " +
                            "quantidade INTEGER NOT NULL, " +
                            "dataHora INTEGER NOT NULL, " +
                            "status INTEGER NOT NULL DEFAULT 0" +
                            ")"
            );
            db.execSQL("CREATE INDEX IF NOT EXISTS index_encomenda_dataHora ON encomenda(dataHora)");
            db.execSQL("CREATE INDEX IF NOT EXISTS index_encomenda_status ON encomenda(status)");
        }
    };

}

