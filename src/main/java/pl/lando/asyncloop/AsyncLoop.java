package pl.lando.asyncloop;

import org.aeonbits.owner.ConfigFactory;
import pl.lando.logger.L;

import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncLoop {

    L log = L.instance(System.out::println);

    private final Callback callback;
    private AtomicBoolean isLoopExecuting = new AtomicBoolean(false);
    private AtomicBoolean isLoopBlocked = new AtomicBoolean(false);
    private Thread thread;

    private Cfg cfg;

    @FunctionalInterface
    public static interface Callback {
        public void apply();
    }

    public AsyncLoop(Callback callback) {
        cfg = ConfigFactory.create(Cfg.class);
        this.callback = callback;
    }

    public void initialize() {
        log.debug("[initialize] begin");

        // TODO: initialize restart state callback

        log.debug("[initialize] end");
    }

    public void asyncLoopRestart() {
        // TODO: initialize restart state callback
        execute();
    }

    /**
     * <p>
     *     Force execute in current thread.
     * </p>
     * @throws Exception
     */
    public void forceExecute() throws Exception {
        if(thread != null) {

            try {
                // block main loop
                isLoopBlocked.set(true);

                // wait for main loop to stop
                thread.join();

                try {
                    // execute main logic in same thread
                    executeCallback();
                } finally {
                    // restore main loop
                    isLoopBlocked.set(false);

                    // execute main loop
                    execute();
                }
            } catch (InterruptedException e) {
                log.error("[forceExecute] interrupted", e);
            }

        } else {
            // just execute main loop
            executeCallback();
        }
    }

    public void executeCallback() {
        callback.apply();
    }

    public void execute() {
        if(isLoopExecuting.get()) {
            log.debug("[execute] already enabled");
        } else {
            isLoopExecuting.set(true);

            // TODO: if is first iteration then initialize() here

            log.debug("[execute] starting loop");
            thread = new Thread() {
                public void run() {
                    log.debug("[execute] start in thread {}", Thread.currentThread().getName());

                    Integer interval = cfg.interval();
                    log.debug("[execute] interval = {}", interval);

                    if(interval < 0) {
                        log.debug("[execute] stopping and restarting state");
                        // TODO: restart state
                        isLoopExecuting.set(false);
                        return;
                    }

                    try {
                        Thread.sleep(interval);
                        log.debug("[execute] executing ...", interval);
                        executeCallback();
                        isLoopExecuting.set(false);

                        if(! isLoopBlocked.get()) {
                            execute();
                        } else {
                            log.warn("[execute] loop is blocked, not executing next loop");
                        }
                    } catch(InterruptedException ex) {
                        isLoopExecuting.set(false);
                        log.error("[execute] interrupted", ex);
                    } catch(Exception ex) {
                        isLoopExecuting.set(false);
                        log.error("[execute] error", ex);
                    }

                    log.debug("[execute] end in thread {}", Thread.currentThread().getName());
                }
            };

            thread.start();
        }
    }


}
