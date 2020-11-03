package septogeddon.pluginquery;

import septogeddon.pluginquery.utils.QueryUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class QueryCompletableFuture<T> extends QueryFutureAdapter<T> {

    private final CompletableFuture<T> future = new CompletableFuture<>();

    @Override
    public void complete(T result) {
        future.complete(result);
        super.complete(result);
    }

    @Override
    public void completeExceptionally(Throwable cause) {
        future.completeExceptionally(cause);
        super.completeExceptionally(cause);
    }

    @Override
    public void joinThread() {
        try {
            future.get();
        } catch (Throwable t) {
        }
    }

    @Override
    public void joinThread(long timeout) {
        try {
            future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (Throwable t) {
            QueryUtil.Throw(t);
        }
    }

}
