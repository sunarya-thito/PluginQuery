package septogeddon.pluginquery;

import org.yaml.snakeyaml.Yaml;
import septogeddon.pluginquery.api.QueryConfiguration;
import septogeddon.pluginquery.api.QueryConfigurationKey;
import septogeddon.pluginquery.api.QueryContext;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class YamlQueryConfiguration implements QueryConfiguration {

    public static final String HEADER =
            "#==========================================================\r\n" +
                    "#                PLUGIN QUERY CONFIGURATION\r\n" +
                    "#----------------------------------------------------------\r\n";
    public static final String FOOTER =
            "#----------------------------------------------------------\r\n" +
                    "# See this wiki for configuration explanation:\r\n" +
                    "# https://sunaryayalasatriathito.gitbook.io/pluginquery/configuration\r\n" +
                    "#----------------------------------------------------------";
    protected Map<String, Object> map = new LinkedHashMap<>();

    public YamlQueryConfiguration() {
        setOption(QueryContext.CONNECTION_THROTTLE, 1500);
        setOption(QueryContext.RECONNECT_DELAY, 1500);
        setOption(QueryContext.LOCK, false);
        setOption(QueryContext.IP_WHITELIST, new ArrayList<>());
        setOption(QueryContext.CONNECTION_LIMIT, 1);
        setOption(QueryContext.MAX_RECONNECT_TRY, -1);
    }

    public static void saveDefaultConfig(File file) throws IOException {
        if (file.getParentFile() != null) file.getParentFile().mkdirs();
        if (!file.exists()) {
            save(file, new YamlQueryConfiguration());
        }
    }

    public static void save(File file, YamlQueryConfiguration configuration) throws IOException {
        Yaml yaml = new Yaml();
        String dumped = yaml.dumpAsMap(configuration.map);
        if (!dumped.endsWith("\n")) {
            dumped += "\n";
        }
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(
                    HEADER
                            +
                            dumped
                            +
                            FOOTER
            );
        }
    }

    @Override
    public <T> T getOption(QueryConfigurationKey<T> key) {
        Object value = map.get(key.name());
        return key.get(value);
    }

    @Override
    public <T> void setOption(QueryConfigurationKey<T> key, T value) {
        Object set = key.set(value);
        if (set != null) {
            map.put(key.name(), value);
        } else map.remove(key.name());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadConfiguration(File file) throws IOException {
        if (!file.exists()) saveDefaultConfig(file);
        Yaml yaml = new Yaml();
        try (FileReader reader = new FileReader(file)) {
            map.putAll(yaml.loadAs(reader, map.getClass()));
        }
    }

    @Override
    public void saveConfiguration(File file) throws IOException {
        save(file, this);
    }

}
