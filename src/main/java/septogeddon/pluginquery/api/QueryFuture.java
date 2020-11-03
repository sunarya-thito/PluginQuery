package septogeddon.pluginquery.api;

import java.util.function.Consumer;

/**
 * Future handling
 * @author Thito Yalasatria Sunarya
 *
 * @param <T> Type returned in the future
 */
public interface QueryFuture<T> {

    /**
     * Check whether the task is done executed no matter what happen to the task
     * @return
     */
    boolean isDone();

    /**
     * Check whether the task is successfully executed
     * @return
     */
    boolean isSuccess();

    /**
     * An error that caused the task failed to do its job
     * @return
     */
    Throwable getCause();

    /**
     * A result from current finished task
     * @return
     */
    T getResult();

    /**
     * Force to wait current thread until the task finished
     */
    void joinThread();

    /**
     * Force to wait current thread until the task finished, will ignore if its too long specified by timeout
     * @param timeout
     */
    void joinThread(long timeout);

    /**
     * Add future listener
     * @param listener
     */
    void addListener(Consumer<QueryFuture<T>> listener);

    /**
     * Consume the result when the task successfully executed
     * @param consumer
     * @return the same instance
     */
    default QueryFuture<T> thenAccept(Consumer<T> consumer) {
        this.addListener(future -> {
            if (future.isSuccess()) {
                consumer.accept(future.getResult());
            }
        });
        return this;
    }

    /**
     * Remove future listener
     * @param listener
     */
    void removeListener(Consumer<QueryFuture<T>> listener);

    /**
     * Print error stack trace if available
     */
    default void printStackTrace() {
        Throwable cause = getCause();
        if (cause != null) cause.printStackTrace();
    }

}
