package cn.edu.sdu.online.isdu.app;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 自定义线程池
 */

public class ThreadPool {

    private static ExecutorService cachedThreadPool =
            Executors.newCachedThreadPool();

    public static void execute(Runnable runnable) {
        cachedThreadPool.execute(runnable);
    }
}
