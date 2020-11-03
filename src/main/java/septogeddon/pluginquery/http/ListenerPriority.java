package septogeddon.pluginquery.http;

public enum ListenerPriority {
    READ, WRITE;
    public boolean allowWrite() {
        return WRITE == this;
    }
}
