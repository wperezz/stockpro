package com.example.estoqueloja.util;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppExecutors {
    private static AppExecutors instance;
    private final ExecutorService io;
    private final Handler main;

    private AppExecutors() {
        io = Executors.newSingleThreadExecutor();
        main = new Handler(Looper.getMainLooper());
    }

    public static AppExecutors get() {
        if (instance == null) instance = new AppExecutors();
        return instance;
    }

    public ExecutorService io() { return io; }
    public Handler main() { return main; }
}
