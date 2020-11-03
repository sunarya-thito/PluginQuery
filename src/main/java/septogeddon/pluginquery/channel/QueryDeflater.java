package septogeddon.pluginquery.channel;

import septogeddon.pluginquery.QueryChannelHandler;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;

public class QueryDeflater extends QueryChannelHandler {

    public QueryDeflater() {
        super(QueryContext.HANDLER_DEFLATER);
    }

    public static byte[] compress(byte[] data) throws IOException {
        if (data.length <= 0) return data;
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        deflater.finish();
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        byte[] output = outputStream.toByteArray();
        return output;
    }

    @Override
    public byte[] onSending(QueryConnection connection, byte[] bytes) throws Exception {
        return super.onSending(connection, compress(bytes));
    }

}
