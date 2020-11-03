package septogeddon.pluginquery.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.mapping;

public class ProtocolQuery {

    private Map<String, List<String>> queryValues;

    public ProtocolQuery(String query) {
        queryValues = splitQuery(query);
    }

    public String getValue(String key) {
        List<String> values = queryValues.get(key);
        if (values != null && !values.isEmpty()) {
            return values.get(0);
        }
        return null;
    }

    public List<String> getValues(String key) {
        return new ArrayList<>(queryValues.get(key));
    }

    private static Map<String, List<String>> splitQuery(String query) {
        if (query == null || query.isEmpty()) return Collections.emptyMap();
        return Arrays.stream(query.split("&"))
                .map(ProtocolQuery::splitQueryParameter)
                .collect(
                        Collectors.groupingBy(
                                AbstractMap.SimpleImmutableEntry::getKey,
                                LinkedHashMap::new, mapping(Map.Entry::getValue, toList())));
    }

    private static AbstractMap.SimpleImmutableEntry<String, String> splitQueryParameter(String it) {
        final int idx = it.indexOf("=");
        final String key = idx > 0 ? it.substring(0, idx) : it;
        final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
        try {
            return new AbstractMap.SimpleImmutableEntry<>(
                    URLDecoder.decode(key, "UTF-8"),
                    URLDecoder.decode(value, "UTF-8")
            );
        } catch (UnsupportedEncodingException exception) {
            throw new RuntimeException(exception); // shouldn't be thrown
        }
    }
}
