package septogeddon.pluginquery.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * The client, or the browser who requested the http. This is also an OutputStream.
 */
public class ProtocolClient extends OutputStream {
    private String version;
    private int responseCode = 200;
    private String responseText = "OK";
    private ChannelHandlerContext context;
    private HTTPContext httpContext;
    private boolean startWrite;

    public ProtocolClient(String version, HTTPContext httpContext, ChannelHandlerContext context) {
        this.version = version;
        this.context = context;
        this.httpContext = httpContext;
    }

    /**
     * Get the protocol version. (i.e. HTTP/1.1)
     * @return the protocol version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get the response code. (i.e. 200 for OK)
     * @return the code
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * Get the response text. (i.e. "OK" for response code 200)
     * @return the text
     */
    public String getResponseText() {
        return responseText == null ? "" : responseText;
    }

    /**
     * Set specific version for the response
     * @param version the protocol version
     */
    public void setVersion(String version) {
        checkWrite();
        this.version = version;
    }

    /**
     * Set the response code for the response
     * @param responseCode the code
     */
    public void setResponseCode(int responseCode) {
        checkWrite();
        this.responseCode = responseCode;
    }

    /**
     * Set the response text (description)
     * @param responseText the text description
     */
    public void setResponseText(String responseText) {
        checkWrite();
        this.responseText = responseText;
    }

    boolean isStartWrite() {
        return startWrite;
    }

    private Map<String, HTTPHeader> headers = new HashMap<>();

    /**
     * Map of headers
     * @return headers
     */
    public Map<String, HTTPHeader> getHeaders() {
        return new HashMap<>(headers);
    }

    /**
     * Get the HTTP Context for this client request
     * @return HTTPContext instance
     */
    public HTTPContext getHTTPContext() {
        return httpContext;
    }

    /**
     * Set header for this response
     * @param key the header name
     * @param value the header value
     */
    public void setHeader(String key, Object value) {
        checkWrite();
        key = key.toLowerCase();
        if (value == null) {
            headers.remove(key);
            return;
        }
        headers.put(key, HTTPHandler.parseHeader(key, value));
    }

    /**
     * Get header value
     * @param name the header name
     * @return the header value
     */
    public HTTPHeader getHeader(String name) {
        return headers.get(name);
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
            for (Map.Entry<String, HTTPHeader> entry : getHeaders().entrySet()) {
                builder.append(entry.getKey());
                builder.append(": ");
                builder.append(entry.getValue());
                builder.append("\r\n");
            }
            builder.append("\r\n");
            write(builder.toString().getBytes());
        }
    }

    /**
     * Initialize and send HTTP response and write byte to the memory buffer. Once this method called,
     * you cannot modify the response header (e.g. {@link #setHeader(String, Object)}, {@link #setResponseCode(int)}, etc)
     * @param b the byte
     */
    @Override
    public void write(int b) {
        initWrite();
        writer.write(b);
    }

    /**
     * Initialize and send HTTP response and write the bytes to the memory buffer. Once this method called,
     * you cannot modify the response header (e.g. {@link #setHeader(String, Object)}, {@link #setResponseCode(int)}, etc)
     * @param b the bytes
     */
    @Override
    public void write(byte[] b) {
        initWrite();
        writer.write(b, 0, b.length);
    }

    /**
     * Initialize and send HTTP response and write the bytes to the memory buffer. Once this method called,
     * you cannot modify the response header (e.g. {@link #setHeader(String, Object)}, {@link #setResponseCode(int)}, etc)
     * @param b the byte array
     * @param off the offset
     * @param len the array length
     */
    @Override
    public void write(byte[] b, int off, int len) {
        initWrite();
        writer.write(b, off, len);
    }

    /**
     * Send the buffer to the client and flush it from the memory
     */
    @Override
    public void flush() {
        if (writer != null && writer.size() > 0) {
            context.writeAndFlush(Unpooled.copiedBuffer(writer.toByteArray()));
            writer.reset();
        }
    }

    /**
     * Send HTTP response to the client and {@link #flush()} the buffer
     */
    @Override
    public void close() {
        initWrite();
        flush();
    }
}
