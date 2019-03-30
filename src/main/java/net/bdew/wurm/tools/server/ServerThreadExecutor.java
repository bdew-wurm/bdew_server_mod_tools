package net.bdew.wurm.tools.server;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides an {@link java.util.concurrent.ExecutorService} that runs stuff on main server thread
 * Code can be queued to run through execute/submit/etc.
 * (see {@link java.util.concurrent.Executor} and {@link java.util.concurrent.ExecutorService} documentation)
 */
public class ServerThreadExecutor extends AbstractExecutorService {
    private ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();
    public static final ServerThreadExecutor INSTANCE = new ServerThreadExecutor();
    private static final Logger logger = Logger.getLogger("ServerThreadExecutor");

    private ServerThreadExecutor() {
    }

    public void tick() {
        while (!queue.isEmpty()) {
            Runnable runnable = queue.poll();
            if (runnable != null) {
                try {
                    runnable.run();
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "Error in scheduled task", t);
                }
            }
        }
    }

    @Override
    public void execute(Runnable command) {
        queue.add(command);
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Runnable> shutdownNow() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }
}
