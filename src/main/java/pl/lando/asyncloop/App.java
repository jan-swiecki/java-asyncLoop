package pl.lando.asyncloop;

import pl.lando.logger.L;

public class App {

    static L l = L.sout();

    public static class State {
        public Integer x = 0;
    }

    public static void main(String[] args) throws Exception {
        AsyncLoop asyncLoop = new AsyncLoop<State>(new State(), (state)->{
            state.x++;
            l.d("yeah -> "+state.x);
        }).initialize();
        asyncLoop.execute();

        Thread.sleep(2500);
        l.d("attempting to stop");
        asyncLoop.stop();
    }
}
