package septogeddon.pluginquery.channel;

import septogeddon.pluginquery.QueryChannelHandler;
import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class QueryInflater extends QueryChannelHandler {

    public QueryInflater() {
        super(QueryContext.HANDLER_INFLATER);
    }

    public static byte[] decompress(byte[] data) throws IOException, DataFormatException {
        if (data.length <= 0) return data;
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        byte[] output = outputStream.toByteArray();
        return output;
    }

    @Override
    public byte[] onReceiving(QueryConnection connection, byte[] bytes) throws Exception {
        return super.onReceiving(connection, decompress(bytes));
    }
}
