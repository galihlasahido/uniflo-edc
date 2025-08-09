package id.uniflo.uniedc.util;


import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Thread pool manager for executing tasks in a background thread.
 * This class provides a singleton instance of a thread pool executor.
 */
public class ThreadPoolManager {
    private static final String TAG = "ThreadPoolManager";

    private ThreadPoolExecutor executor;

    private ThreadPoolManager() {
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        executor = new ThreadPoolExecutor(
                corePoolSize,
                Integer.MAX_VALUE,
                1,
                TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    public static ThreadPoolManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    public void execute(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        executor.execute(runnable);
    }

    public void remove(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        executor.remove(runnable);
    }

    private static class LazyHolder {
        public static final ThreadPoolManager INSTANCE = new ThreadPoolManager();
    }

}
