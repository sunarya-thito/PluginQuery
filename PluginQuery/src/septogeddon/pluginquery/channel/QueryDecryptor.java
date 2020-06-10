package septogeddon.pluginquery.channel;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

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
		return super.onSending(connection, bytes);
	}
	
	@Override
	public void onUncaughtException(QueryConnection connection, Throwable thrown) throws Exception {
		if (thrown instanceof BadPaddingException || thrown instanceof IllegalBlockSizeException) {
			connection.disconnect();
			return;
		}
		super.onUncaughtException(connection, thrown);
	}

}
