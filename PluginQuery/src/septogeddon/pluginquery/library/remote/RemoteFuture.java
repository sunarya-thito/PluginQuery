package septogeddon.pluginquery.library.remote;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryFuture;

/***
 * Wrapped completable future and QueryFutureListener
 * @author Thito Yalasatria Sunarya
 *
 */
public class RemoteFuture implements Consumer<QueryFuture<QueryConnection>> {

	private CompletableFuture<Object> future = new CompletableFuture<>();
	
	/***
	 * Complete the future
	 * @param obj
	 */
	public void complete(Object obj) {
		future.complete(obj);
	}
	
	/***
	 * Indicate that the task failed to execute
	 * @param thrown
	 */
	public void completeExceptionally(Throwable thrown) {
		future.completeExceptionally(thrown);
	}
	
	/***
	 * Wait until the task done
	 * @return the expected object
	 * @throws InterruptedException if the thread got interrupted
	 * @throws ExecutionException if the task failed to execute
	 */
	public Object get() throws InterruptedException, ExecutionException {
		return future.get();
	}
	
	/***
	 * Wait until the task done
	 * @param timeout timeout time
	 * @param unit the time unit
	 * @return the expected object
	 * @throws InterruptedException if the thread got interrupted
	 * @throws ExecutionException if the task failed to execute
	 * @throws TimeoutException if the wait time reaches the timeout time
	 */
	public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return future.get(timeout, unit);
	}

	/***
	 * Dispatch for QueryFutureListener event
	 */
	@Override
	public void accept(QueryFuture<QueryConnection> t) {
		if (!t.isSuccess()) {
			completeExceptionally(t.getCause());
		}
	}
	
}
