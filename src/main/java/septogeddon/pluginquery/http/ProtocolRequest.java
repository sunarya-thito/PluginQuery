package septogeddon.pluginquery.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;

import java.util.Map;

/**
 * The request data sent from the client/browser. This is also an InputStream.
 */
public class ProtocolRequest extends ByteBufInputStream {
    private ProtocolMethod method;
    private ProtocolPath path;
    private String version;
    private Map<String, HTTPHeader> headers;
    private HTTPContext context;

    public ProtocolRequest(HTTPContext context, ProtocolMethod method, ProtocolPath path, String version, Map<String, HTTPHeader> headers, ByteBuf content) {
        super(content);
        this.context = context;
        this.method = method;
        this.path = path;
        this.version = version;
        this.headers = headers;
    }

    /**
     * Get the HTTP context
     * @return HTTPContext instance
     */
    public HTTPContext getHTTPContext() {
        return context;
    }

    /**
     * Get the header value
     * @param name the header name
     * @return the header value
     */
    public HTTPHeader getHeaderValue(String name) {
        return headers.get(name);
    }

    /**
     * Get the method of this request
     * @return HTTP method
     */
    public ProtocolMethod getMethod() {
        return method;
    }

    /**
     * Get the path of this request
     * @return HTTP path URL
     */
    public ProtocolPath getPath() {
        return path;
    }

    /**
     * Get the protocol version of this request (e.g. HTTP/1.1)
     * @return the protocol version
     */
    public String getVersion() {
        return version;
    }
}
