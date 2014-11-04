package pl.lando.asyncloop;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncLoop {

    Logger log = LoggerFactory.getLogger(AsyncLoop.class);

    private AtomicBoolean isLoopExecuting = new AtomicBoolean(false);
    private AtomicBoolean isLoopBlocked = new AtomicBoolean(false);
    private Thread thread;

    private Cfg cfg;

    public AsyncLoop() {
        cfg = ConfigFactory.create(Cfg.class);
    }

    public void initialize() {
        log.debug("[initialize] begin");

        // initialize callback

        log.debug("[initialize] end");
    }

    public void asyncLoopRestart() {
        // restart state
        asyncLoop();
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
                    execute();
                } finally {
                    // restore main loop
                    isLoopBlocked.set(false);

                    // execute main loop
                    asyncLoop();
                }
            } catch (InterruptedException e) {
                log.error("[forceExecute] interrupted", e);
            }

        } else {
            // just execute main loop
            execute();
        }
    }

    public void execute() {

    }

    public void asyncLoop() {
        if(isLoopExecuting.get()) {
            log.debug("[asyncLoop] already enabled");
        } else {
            isLoopExecuting.set(true);

            // TODO: if is first iteration then initialize() here

            log.debug("[asyncLoop] starting loop");
            thread = new Thread() {
                public void run() {
                    log.debug("[asyncLoop] start in thread {}", Thread.currentThread().getName());

                    Integer interval = cfg.interval();
                    log.debug("[asyncLoop] interval = {}", interval);

                    if(interval < 0) {
                        log.debug("[asyncLoop] stopping and restarting state");
                        // TODO: restart state
                        isLoopExecuting.set(false);
                        return;
                    }

                    try {
                        Thread.sleep(interval);
                        log.debug("[asyncLoop] executing ...", interval);
                        execute();
                        isLoopExecuting.set(false);

                        if(! isLoopBlocked.get()) {
                            asyncLoop();
                        } else {
                            log.warn("[asyncLoop] loop is blocked, not executing next loop");
                        }
                    } catch(InterruptedException ex) {
                        isLoopExecuting.set(false);
                        log.error("[asyncLoop] interrupted", ex);
                    } catch(Exception ex) {
                        isLoopExecuting.set(false);
                        log.error("[asyncLoop] error", ex);
                    }

                    log.debug("[asyncLoop] end in thread {}", Thread.currentThread().getName());
                }
            };

            thread.start();
        }
    }


}
