package septogeddon.pluginquery.channel;

import javax.crypto.Cipher;

import septogeddon.pluginquery.QueryChannelHandler;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;

public class QueryDecryptor extends QueryChannelHandler {

	private Cipher cipher;
	public QueryDecryptor(Cipher cipher) {
		super(QueryContext.HANDLER_DECRYPTOR);
		this.cipher = cipher;
	}
	
	@Override
	public byte[] onReceiving(QueryConnection connection, byte[] bytes) throws Exception {
		bytes = cipher.doFinal(bytes);
		return super.onReceiving(connection, bytes);
	}
	
	@Override
	public void onCaughtException(QueryConnection connection, Throwable thrown) throws Exception {
		thrown.printStackTrace();
	}
	
}
