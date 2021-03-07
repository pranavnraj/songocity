package com.songbirds.concurrency;

import com.songbirds.util.AppConstants;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SongbirdExecutorService {

    private static ExecutorService service = Executors.newFixedThreadPool(AppConstants.EXECUTOR_THREAD_COUNT);

    private SongbirdExecutorService() {}

    public static ExecutorService getExecutorService() {
        return service;
    }

}
