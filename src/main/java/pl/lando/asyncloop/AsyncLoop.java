package pl.lando.asyncloop;

import org.aeonbits.owner.ConfigFactory;
import pl.lando.logger.L;

import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncLoop<T> {

    L log = L.instance(System.out::println);

    private CallbackWithState<T> callbackWithState;
    private Callback callback;
    private AtomicBoolean isStopped = new AtomicBoolean(false);
    private AtomicBoolean isLoopExecuting = new AtomicBoolean(false);
    private AtomicBoolean isLoopBlocked = new AtomicBoolean(false);
    private Thread thread;
    private T state;

    private Cfg cfg;

    @FunctionalInterface
    public static interface Callback {
        public void apply();
    }

    @FunctionalInterface
    public static interface CallbackWithState<T> {
        public void apply(T state);
    }

    public AsyncLoop(Callback callback) {
        this.callback = callback;
        init();
    }

    public AsyncLoop(T state, CallbackWithState<T> callback) {
        this.state = state;
        this.callbackWithState = callback;
        init();
    }

    private void init() {
        cfg = ConfigFactory.create(Cfg.class);
    }

    public AsyncLoop initialize() {
        log.debug("[initialize] begin");

        // TODO: initialize restart state callback

        log.debug("[initialize] end");

        return this;
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
        if(callbackWithState != null) {
            callbackWithState.apply(state);
        } else {
            callback.apply();
        }
    }

    private boolean checkStopped() {
        if(isStopped.get()) {
            log.debug("[checkStopped] is stopped, stopping and restarting state");
            isLoopExecuting.set(false);
            // TODO: restart state
            return true;
        } else {
            return false;
        }
    }

    public void execute() {
        if(isLoopExecuting.get()) {
            log.debug("[execute] already enabled");
        } else {
            isLoopExecuting.set(true);

            if(checkStopped()) return;

            // TODO: if is first iteration then initialize() here

            log.debug("[execute] starting loop");
            thread = new Thread() {
                public void run() {
                    log.debug("[execute] start in thread {}", Thread.currentThread().getName());

                    Integer interval = cfg.interval();
                    log.debug("[execute] interval = {}", interval);

                    assert interval > 0;

                    if(checkStopped()) return;

                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        log.error("[execute] interrupted");
                        isStopped.set(true);
                    }

                    if(checkStopped()) return;

                    try {
                        log.debug("[execute] executing ...", interval);
                        executeCallback();
                        isLoopExecuting.set(false);

                        if(! isLoopBlocked.get()) {
                            execute();
                        } else {
                            log.warn("[execute] loop is blocked, not executing next loop");
                        }
                    } catch(Exception ex) {
                        isLoopExecuting.set(false);
                        log.error("[execute] error {}", ex);
                    }

                    log.debug("[execute] end in thread {}", Thread.currentThread().getName());
                }
            };

            thread.start();
        }
    }

    public void stop() {
        isStopped.set(true);
        thread.interrupt();
    }
}
