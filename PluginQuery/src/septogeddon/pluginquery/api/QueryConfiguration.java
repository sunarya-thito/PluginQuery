package septogeddon.pluginquery.api;

import java.io.File;
import java.io.IOException;

public interface QueryConfiguration {

	public <T> T getOption(QueryConfigurationKey<T> key);
	public <T> void setOption(QueryConfigurationKey<T> key, T value);
	/**
	 * Load configuration from a file (YAML)
	 * @param file
	 */
	public void loadConfiguration(File file) throws IOException;
	/***
	 * Dump configuration into a file (YAML)
	 * @param file
	 */
	public void saveConfiguration(File file) throws IOException;
	
}
