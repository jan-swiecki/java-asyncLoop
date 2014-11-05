package pl.lando.asyncloop;

public class AsyncLoopStateful<T> extends AsyncLoop {
    @FunctionalInterface
    public static interface CallbackWithState<T> {
        public void apply(T state);
    }

    private final CallbackWithState<T> callbackWithState;
    private final T state;

    public AsyncLoopStateful(T state, CallbackWithState<T> callback) {
        super();
        this.state = state;
        this.callbackWithState = callback;
    }

    public void executeCallback() {
        callbackWithState.apply(state);
    }
}
