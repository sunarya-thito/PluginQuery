package septogeddon.pluginquery.channel;

import septogeddon.pluginquery.QueryChannelHandler;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;

import javax.crypto.Cipher;

public class QueryDecryptor extends QueryChannelHandler {

    private final Cipher cipher;

    public QueryDecryptor(Cipher cipher) {
        super(QueryContext.HANDLER_DECRYPTOR);
        this.cipher = cipher;
    }

    @Override
    public byte[] onReceiving(QueryConnection connection, byte[] bytes) throws Exception {
        if (bytes.length > 0) bytes = cipher.doFinal(bytes);
        return super.onReceiving(connection, bytes);
    }

    @Override
    public void onCaughtException(QueryConnection connection, Throwable thrown) throws Exception {
        connection.disconnect();
        super.onCaughtException(connection, thrown);
    }

}
