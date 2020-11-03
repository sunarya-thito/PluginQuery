package septogeddon.pluginquery.http.headertype;

import septogeddon.pluginquery.http.HTTPHeader;

import java.net.HttpCookie;

/**
 * Set-Cookie HTTP header
 */
public class SetCookieHeader implements HTTPHeader {
    private HttpCookie cookie;

    /**
     * Parse Set-Cookie header
     * @param raw the raw header value
     */
    public SetCookieHeader(Object raw) {
        cookie = HttpCookie.parse(String.valueOf(raw)).get(0);
    }

    /**
     * Create a Set-Cookie header
     * @param name the cookie name
     * @param value the cookie value
     */
    public SetCookieHeader(String name, Object value) {
        cookie = new HttpCookie(name, String.valueOf(value));
    }

    /**
     * Create a Set-Cookie header using existing HttpCookie
     * @param cookie
     */
    public SetCookieHeader(HttpCookie cookie) {
        this.cookie = cookie;
    }

    /**
     * The Cookie
     * @return the HttpCookie instance
     */
    public HttpCookie getCookie() {
        return cookie;
    }

    @Override
    public String toString() {
        return cookie.toString();
    }
}
