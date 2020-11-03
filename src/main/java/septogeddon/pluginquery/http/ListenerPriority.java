package septogeddon.pluginquery.http;

/**
 * The priority of listener. It manages which listener should goes first.
 */
public enum ListenerPriority {
    /**
     * All listener that only read request and not writing any response should use this priority
     */
    READ,
    /**
     * All listener that read and/or write a response should use this priority
     */
    WRITE;

    /**
     * Determines whether this priority is allowed to write a response
     * @return true if {@link ListenerPriority#WRITE}
     */
    public boolean allowWrite() {
        return WRITE == this;
    }
}
