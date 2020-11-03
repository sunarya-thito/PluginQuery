package septogeddon.pluginquery.http;

import septogeddon.pluginquery.utils.QueryUtil;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Arrays;

/**
 * The HTTP path
 */
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

    /**
     * The length of the path
     * @return length
     */
    public int length() {
        return path.length;
    }

    /**
     * Get a file/directory in specified index
     * @param index the index
     * @return path name
     */
    public String get(int index) {
        return path[index];
    }

    /**
     * Get the paths
     * @return
     */
    public String[] toArray() {
        return Arrays.copyOf(path, path.length);
    }

    /**
     * Get the query
     * @return
     */
    public ProtocolQuery getQuery() {
        return query;
    }

}
