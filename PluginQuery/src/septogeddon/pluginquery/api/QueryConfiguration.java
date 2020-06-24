package septogeddon.pluginquery.api;

import java.io.File;
import java.io.IOException;

/***
 * Configuration for PluginQuery
 * @author Thito Yalasatria Sunarya
 */
public interface QueryConfiguration {
	
	/***
	 * Get option value from the configuration
	 * @param <T> Anything
	 * @param key the Key holder
	 * @return the value
	 */
	public <T> T getOption(QueryConfigurationKey<T> key);
	/***
	 * Set option value on the configuration
	 * @param <T> Anything
	 * @param key the Key holder
	 * @param value the value
	 */
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
