package septogeddon.pluginquery;

import septogeddon.pluginquery.api.QueryFuture;
import septogeddon.pluginquery.utils.QueryUtil;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public abstract class QueryFutureAdapter<T> implements QueryFuture<T> {

    protected T result;
    protected Throwable cause;
    protected boolean done;
    private final Set<Consumer<QueryFuture<T>>> listeners = ConcurrentHashMap.newKeySet();

    public synchronized void complete(T result) {
        this.result = result;
        done = true;
        fireEvent();
        listeners.clear();
    }

    public synchronized void fireEvent() {
        for (Consumer<QueryFuture<T>> listener : listeners) {
            listener.accept(this);
        }
    }

    public synchronized void completeExceptionally(Throwable cause) {
        this.cause = cause;
        done = true;
        fireEvent();
        listeners.clear();
    }

    @Override
    public synchronized boolean isDone() {
        return done;
    }

    @Override
    public synchronized boolean isSuccess() {
        return cause == null;
    }

    @Override
    public synchronized Throwable getCause() {
        return cause;
    }

    @Override
    public synchronized T getResult() {
        return result;
    }

    @Override
    public synchronized void addListener(Consumer<QueryFuture<T>> listener) {
        QueryUtil.nonNull(listener, "listener");
        if (done) {
            listener.accept(this);
        } else {
            listeners.add(listener);
        }
    }

    @Override
    public synchronized void removeListener(Consumer<QueryFuture<T>> listener) {
        QueryUtil.nonNull(listener, "listener");
        listeners.remove(listener);
    }

}
