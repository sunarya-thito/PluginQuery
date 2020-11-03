package septogeddon.pluginquery.http.headertype;

import septogeddon.pluginquery.http.HTTPHeader;

/**
 * Default (Global) or Unknown (Unregistered) header
 */
public class RawHeader implements HTTPHeader {

    private String value;

    /**
     * Create an Unknown header
     * @param value the header value
     */
    public RawHeader(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
