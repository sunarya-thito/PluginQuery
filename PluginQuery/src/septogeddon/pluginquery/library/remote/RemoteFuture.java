package septogeddon.pluginquery.library.remote;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryFuture;

public class RemoteFuture implements Consumer<QueryFuture<QueryConnection>> {

	private Class<?> hint;
	private CompletableFuture<Object> future = new CompletableFuture<>();
	private ReferenceHandler handler;
	public RemoteFuture(ReferenceHandler handler) {
		this.handler = handler;
	}
	
	public ReferenceHandler getHandler() {
		return handler;
	}
	
	public void complete(Object obj) {
		future.complete(obj);
	}
	
	public void completeExceptionally(Throwable thrown) {
		future.completeExceptionally(thrown);
	}
	
	public Class<?> getHint() {
		return hint;
	}
	
	public Object get() throws InterruptedException, ExecutionException {
		return future.get();
	}
	
	public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return future.get(timeout, unit);
	}

	@Override
	public void accept(QueryFuture<QueryConnection> t) {
		if (!t.isSuccess()) {
			completeExceptionally(t.getCause());
		}
	}
	
}
