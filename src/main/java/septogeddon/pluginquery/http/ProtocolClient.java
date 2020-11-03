package septogeddon.pluginquery.http;

import io.netty.buffer.AbstractByteBuf;
import io.netty.buffer.AbstractByteBufAllocator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ProtocolClient extends OutputStream {
    private String version;
    private int responseCode = 200;
    private String responseText = "OK";
    private ChannelHandlerContext context;
    private boolean startWrite;

    public ProtocolClient(String version, ChannelHandlerContext context) {
        this.version = version;
        this.context = context;
    }

    public String getVersion() {
        return version;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseText() {
        return responseText == null ? "" : responseText;
    }

    public void setVersion(String version) {
        checkWrite();
        this.version = version;
    }

    public void setResponseCode(int responseCode) {
        checkWrite();
        this.responseCode = responseCode;
    }

    public void setResponseText(String responseText) {
        checkWrite();
        this.responseText = responseText;
    }

    boolean isStartWrite() {
        return startWrite;
    }

    private Map<String, String> headers = new HashMap<>();

    public Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }

    public void setHeader(String key, Object value) {
        checkWrite();
        if (value == null) {
            headers.remove(key.toLowerCase());
            return;
        }
        headers.put(key.toLowerCase(), String.valueOf(value));
    }

    public String getHeader(String value) {
        return headers.get(value);
    }

    ByteArrayOutputStream writer;

    void checkWrite() {
        if (startWrite) throw new IllegalStateException("Already written");
    }

    void initWrite() {
        if (!startWrite) {
            writer = new ByteArrayOutputStream();
            startWrite = true;
            StringBuilder builder = new StringBuilder();
            // HEAD LINE
            builder.append(version);
            builder.append(' ');
            builder.append(responseCode);
            builder.append(' ');
            builder.append(getResponseText());
            builder.append("\r\n");
            // HEADERS
            for (Map.Entry<String, String> entry : getHeaders().entrySet()) {
                builder.append(entry.getKey());
                builder.append(": ");
                builder.append(entry.getValue());
                builder.append("\r\n");
            }
            builder.append("\r\n");
            write(builder.toString().getBytes());
        }
    }

    @Override
    public void write(int b) {
        initWrite();
        writer.write(b);
    }

    @Override
    public void write(byte[] b) {
        initWrite();
        writer.write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        initWrite();
        writer.write(b, off, len);
    }

    @Override
    public void flush() {
        if (writer != null && writer.size() > 0) {
            context.writeAndFlush(Unpooled.copiedBuffer(writer.toByteArray()));
            writer.reset();
        }
    }

    @Override
    public void close() {
        initWrite();
        flush();
    }
}
