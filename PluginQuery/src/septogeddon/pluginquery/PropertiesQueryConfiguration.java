package septogeddon.pluginquery;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import septogeddon.pluginquery.api.QueryConfiguration;
import septogeddon.pluginquery.api.QueryConfigurationKey;
import septogeddon.pluginquery.api.QueryContext;

public class PropertiesQueryConfiguration implements QueryConfiguration {
	public static final String HEADER = 
			"==========================================================\r\n" + 
			"#                PLUGIN QUERY CONFIGURATION\r\n" + 
			"#----------------------------------------------------------\r\n" + 
			"# See this wiki for configuration explanation:\r\n" + 
			"# https://sunaryayalasatriathito.gitbook.io/pluginquery/configuration\r\n" + 
			"#----------------------------------------------------------";
	private Properties properties = new Properties();
	
	public PropertiesQueryConfiguration() {
		setOption(QueryContext.CONNECTION_THROTTLE, 1500);
		setOption(QueryContext.RECONNECT_DELAY, 1500);
		setOption(QueryContext.LOCK, false);
		setOption(QueryContext.IP_WHITELIST, new ArrayList<>());
		setOption(QueryContext.CONNECTION_LIMIT, 1);
		setOption(QueryContext.MAX_RECONNECT_TRY, -1);
	}
	
	@Override
	public <T> T getOption(QueryConfigurationKey<T> key) {
		return key.get(properties.getProperty(key.name()));
	}

	@Override
	public <T> void setOption(QueryConfigurationKey<T> key, T value) {
		Object set = key.set(value);
		if (set != null) {
			if (set instanceof List) {
				ArrayList<String> strings = new ArrayList<>();
				for (int i = 0; i < ((List<?>)set).size(); i++) {
					Object get = ((List<?>) set).get(i);
					if (get != null) strings.add(get.toString());
				}
				// Comma-Separated-Values
				properties.setProperty(key.name(), String.join(",", strings));
			} else {
				properties.setProperty(key.name(), set.toString());
			}
		} else {
			properties.remove(key.name());
		}
	}

	@Override
	public void loadConfiguration(File file) throws IOException {
		properties.load(new FileReader(file));
	}

	@Override
	public void saveConfiguration(File file) throws IOException {
		properties.store(new FileWriter(file), HEADER);
	}

}
