package septogeddon.pluginquery.http;

import septogeddon.pluginquery.utils.QueryUtil;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;

public class ProtocolPath {
    private String[] path;
    private ProtocolQuery query;
    public ProtocolPath(String url) {
        URI uri = URI.create(url);
        path = uri.getRawPath().split("/");
        for (int i = 0; i < path.length; i++) {
            try {
                path[i] = URLDecoder.decode(path[i], "UTF-8");
            } catch (UnsupportedEncodingException exception) {
                QueryUtil.Throw(exception);
            }
        }
        query = new ProtocolQuery(uri.getRawQuery());
    }

    public int length() {
        return path.length;
    }

    public String get(int index) {
        return path[index];
    }

    public String[] toArray() {
        return Arrays.copyOf(path, path.length);
    }

    public ProtocolQuery getQuery() {
        return query;
    }

}
