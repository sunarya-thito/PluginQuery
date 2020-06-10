package septogeddon.pluginquery.channel;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;

import septogeddon.pluginquery.QueryChannelHandler;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;

public class QueryDeflater extends QueryChannelHandler {

	public QueryDeflater() {
		super(QueryContext.HANDLER_DEFLATER);
	}
	
	@Override
	public byte[] onSending(QueryConnection connection, byte[] bytes) throws Exception {
		Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
		deflater.setInput(bytes);
		deflater.finish();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		int len;
		byte[] buf = new byte[1024];
		while ((len = deflater.deflate(buf, 0, buf.length)) != -1) {
			output.write(buf, 0, len);
		}
		bytes = output.toByteArray();
		deflater.end();
		return super.onSending(connection, bytes);
	}

}
