package pl.lando.asyncloop;

public class AsyncLoopStateless extends AsyncLoop {
    private Callback callback;

    @FunctionalInterface
    public static interface Callback {
        public void apply();
    }

    public AsyncLoopStateless(Callback callback) {
        super();
        this.callback = callback;
    }

    @Override
    void executeCallback() {
        callback.apply();
    }

}
