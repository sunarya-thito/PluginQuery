package septogeddon.pluginquery.channel;

import septogeddon.pluginquery.QueryChannelHandler;
import septogeddon.pluginquery.api.QueryContext;

public class QueryThrottle extends QueryChannelHandler {

	public QueryThrottle(long throttle) {
		super(QueryContext.HANDLER_THROTTLE);
	}

}
