package com.example.estoqueloja.data.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.estoqueloja.data.dao.MovDao;
import com.example.estoqueloja.data.dao.ProdutoDao;
import com.example.estoqueloja.data.entity.MovEstoque;
import com.example.estoqueloja.data.entity.Produto;

@Database(entities = {Produto.class, MovEstoque.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ProdutoDao produtoDao();
    public abstract MovDao movDao();
}
