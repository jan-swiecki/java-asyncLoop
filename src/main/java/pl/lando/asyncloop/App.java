package pl.lando.asyncloop;

import pl.lando.logger.L;

public class App {

    static L l = L.sout();

    public static void main(String[] args) throws Exception {
        AsyncLoop asyncLoop = new AsyncLoop(()->{
            l.d("yeah!");
        }).initialize();
        asyncLoop.execute();

        Thread.sleep(2500);
        l.d("attempting to stop");
        asyncLoop.stop();
    }
}
