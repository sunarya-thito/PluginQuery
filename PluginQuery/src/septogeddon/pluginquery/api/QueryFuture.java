package septogeddon.pluginquery.api;

import java.util.function.Consumer;

public interface QueryFuture<T> {

	/***
	 * Check whether the task is done executed no matter what happen to the task
	 * @return
	 */
	public boolean isDone();
	/***
	 * Check whether the task is successfully executed
	 * @return
	 */
	public boolean isSuccess();
	/***
	 * An error that caused the task failed to do its job
	 * @return
	 */
	public Throwable getCause();
	/***
	 * A result from current finished task
	 * @return
	 */
	public T getResult();
	/***
	 * Force to wait current thread until the task finished
	 */
	public void joinThread();
	/***
	 * Force to wait current thread until the task finished, will ignore if its too long specified by timeout
	 * @param timeout
	 */
	public void joinThread(long timeout);
	/***
	 * Add future listener
	 * @param listener
	 */
	public void addListener(Consumer<QueryFuture<T>> listener);
	/***
	 * Consume the result when the task successfully executed
	 * @param consumer
	 */
	public default void thenAccept(Consumer<T> consumer) {
		this.addListener(future->{
			if (future.isSuccess()) {
				consumer.accept(future.getResult());
			}
		});
	}
	/***
	 * Remove future listener
	 * @param listener
	 */
	public void removeListener(Consumer<QueryFuture<T>> listener);
	/***
	 * Print error stack trace if available
	 */
	public default void printStackTrace() {
		Throwable cause = getCause();
		if (cause != null) cause.printStackTrace();
	}

}
