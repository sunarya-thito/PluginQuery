package septogeddon.pluginquery.channel;

import javax.crypto.Cipher;

import septogeddon.pluginquery.QueryChannelHandler;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;

public class QueryEncryptor extends QueryChannelHandler {

	private Cipher cipher;
	public QueryEncryptor(Cipher cipher) {
		super(QueryContext.HANDLER_ENCRYPTOR);
		this.cipher = cipher;
	}
	
	@Override
	public byte[] onSending(QueryConnection connection, byte[] bytes) throws Exception {
		bytes = cipher.doFinal(bytes);
		return super.onSending(connection, bytes);
	}
	
}
