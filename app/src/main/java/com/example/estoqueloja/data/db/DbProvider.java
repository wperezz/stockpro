package com.example.estoqueloja.data.db;

import android.content.Context;

import androidx.room.Room;

public class DbProvider {
    private static AppDatabase db;

    public static AppDatabase get(Context ctx) {
        if (db == null) {
            db = Room.databaseBuilder(ctx.getApplicationContext(),
                            AppDatabase.class, "estoque.db")
                    .addMigrations(AppDatabase.MIGRATION_3_4)
                    .build();
        }
        return db;
    }
}