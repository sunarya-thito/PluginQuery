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

    public void complete(T result) {
        this.result = result;
        done = true;
        fireEvent();
        listeners.clear();
    }

    public void fireEvent() {
        for (Consumer<QueryFuture<T>> listener : listeners) {
            listener.accept(this);
        }
    }

    public void completeExceptionally(Throwable cause) {
        this.cause = cause;
        done = true;
        fireEvent();
        listeners.clear();
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public boolean isSuccess() {
        return cause == null;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

    @Override
    public T getResult() {
        return result;
    }

    @Override
    public void addListener(Consumer<QueryFuture<T>> listener) {
        QueryUtil.nonNull(listener, "listener");
        if (done) {
            listener.accept(this);
        } else {
            listeners.add(listener);
        }
    }

    @Override
    public void removeListener(Consumer<QueryFuture<T>> listener) {
        QueryUtil.nonNull(listener, "listener");
        listeners.remove(listener);
    }

}
