package com.example.estoqueloja.data.db;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.estoqueloja.data.dao.MovDao;
import com.example.estoqueloja.data.dao.ProdutoDao;
import com.example.estoqueloja.data.entity.MovEstoque;
import com.example.estoqueloja.data.entity.Produto;

@Database(entities = {Produto.class, MovEstoque.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ProdutoDao produtoDao();
    public abstract MovDao movDao();

    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE mov_estoque ADD COLUMN vendaId INTEGER NOT NULL DEFAULT 0");
        }
    };
}
