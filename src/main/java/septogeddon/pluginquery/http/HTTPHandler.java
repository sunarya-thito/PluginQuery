package septogeddon.pluginquery.http;

import septogeddon.pluginquery.http.headertype.CookieHeader;
import septogeddon.pluginquery.http.headertype.RawHeader;
import septogeddon.pluginquery.http.headertype.SetCookieHeader;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utilities for PluginQuery HTTP Handler
 */
public class HTTPHandler {
    private static Map<String, Object> headerParsers = new HashMap<>();

    static {
        setParser("Set-Cookie", x -> new SetCookieHeader(x));
        setParser("Cookie", CookieHeader::new);
    }

    /**
     * Set a parser for specific header
     * @param headerName the header name, null for global (all headers)
     * @param factory the HTTPHeader factory
     */
    public static void setParser(String headerName, Function<Object, HTTPHeader> factory) {
        headerParsers.put(headerName.toLowerCase(), factory);
    }

    /**
     * Set a parser for specific header
     * @param headerName the header name, null for global (all headers)
     * @param factory the HTTPHeader factory
     */
    public static void setParser(String headerName, BiFunction<String, Object, HTTPHeader> factory) {
        headerParsers.put(headerName.toLowerCase(), factory);
    }

    /**
     * Parse header raw value into HTTPHeader
     * @param headerName the header name
     * @param rawValue the header value
     * @return parsed HTTPHeader
     */
    public static HTTPHeader parseHeader(String headerName, Object rawValue) {
        if (rawValue instanceof HTTPHeader) return (HTTPHeader) rawValue;
        Object parser = headerParsers.getOrDefault(headerName.toLowerCase(), headerParsers.get(null));
        if (parser != null) {
            if (parser instanceof Function) {
                return ((Function<Object, HTTPHeader>) parser).apply(rawValue);
            }
            if (parser instanceof BiFunction) {
                return ((BiFunction<String, Object, HTTPHeader>) parser).apply(headerName, rawValue);
            }
        }
        return new RawHeader(String.valueOf(rawValue));
    }
}
