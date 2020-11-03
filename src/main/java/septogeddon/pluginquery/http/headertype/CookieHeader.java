package septogeddon.pluginquery.http.headertype;

import septogeddon.pluginquery.http.HTTPHeader;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

/**
 * Cookie HTTP header
 */
public class CookieHeader implements HTTPHeader {

    private String rawValue;
    private List<HttpCookie> cookies;

    /**
     * Parse Cookie header
     * @param rawValue the raw header value
     */
    public CookieHeader(Object rawValue) {
        cookies = HttpCookie.parse(this.rawValue = String.valueOf(rawValue));
    }

    /**
     * The browser cookies
     * @return
     */
    public List<HttpCookie> getCookies() {
        return new ArrayList<>(cookies);
    }

    @Override
    public String toString() {
        return rawValue;
    }
}
