package septogeddon.pluginquery.channel;

import java.io.ByteArrayOutputStream;
import java.util.zip.Inflater;

import septogeddon.pluginquery.QueryChannelHandler;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;

public class QueryInflater extends QueryChannelHandler {

	public QueryInflater() {
		super(QueryContext.HANDLER_INFLATER);
	}
	
	@Override
	public byte[] onReceiving(QueryConnection connection, byte[] bytes) throws Exception {
		Inflater inflater = new Inflater();
		inflater.setInput(bytes);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		int len;
		byte[] buf = new byte[1024];
		while ((len = inflater.inflate(buf, 0, buf.length)) != -1) {
			output.write(buf, 0, len);
		}
		bytes = output.toByteArray();
		inflater.end();
		return super.onReceiving(connection, bytes);
	}
	
}
