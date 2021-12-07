package septogeddon.pluginquery.message;

public class QueryBroadcastMessage extends QueryObject {
    private static final long serialVersionUID = 1L;
    private String message;

    public QueryBroadcastMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
