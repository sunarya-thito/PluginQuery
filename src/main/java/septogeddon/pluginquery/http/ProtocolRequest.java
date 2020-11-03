package septogeddon.pluginquery.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ProtocolRequest {
    private ProtocolMethod method;
    private ProtocolPath path;
    private String version;
    private Map<String, String> headers;
    private ByteBuf buffer;

    public ProtocolRequest(ProtocolMethod method, ProtocolPath path, String version, Map<String, String> headers, ByteBuf content) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.headers = headers;
        this.buffer = content;
    }

    public InputStream getInputStream() {
        return new ByteBufInputStream(buffer);
    }

    public String getHeaderValue(String header) {
        return headers.get(header);
    }

    public ProtocolMethod getMethod() {
        return method;
    }

    public ProtocolPath getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }
}
