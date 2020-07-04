package septogeddon.pluginquery.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/***
 * Configuration key that holds key/path and also a serializer handler
 * @author Thito Yalasatria Sunarya
 *
 * @param <T> The expected value
 */
public interface QueryConfigurationKey<T> {

	/***
	 * The configuration key/path
	 * @return
	 */
	public String name();
	/***
	 * Parse from safe-config object
	 * @param from
	 * @return
	 */
	public T get(Object from);
	/***
	 * Convert to safe-config object
	 * @param t
	 * @return
	 */
	public Object set(T t);
	
	/***
	 * Create new boolean parser
	 * @param key the key
	 * @return the created configuration key
	 */
	public static QueryConfigurationKey<Boolean> newBoolean(String key) {
		return new QueryConfigurationKey<Boolean>() {

			@Override
			public String name() {
				return key;
			}

			@Override
			public Boolean get(Object from) {
				if (from instanceof String) return "true".equals(from);
				return from == Boolean.TRUE;
			}

			@Override
			public Object set(Boolean t) {
				return t;
			}
			
		};
	}
	
	/***
	 * Create new list of string parser
	 * @param key the key
	 * @return the created configuration key
	 */
	public static QueryConfigurationKey<List<String>> newStringList(String key) {
		return new QueryConfigurationKey<List<String>>() {

			@Override
			public String name() {
				return key;
			}

			@Override
			public List<String> get(Object from) {
				List<String> list = new ArrayList<>();
				if (from instanceof List) {
					for (Object o : (List<?>)from) {
						if (o instanceof String) {
							list.add((String)o);
						}
					}
				} else if (from instanceof String) {
					// Comma-Separated-Value
					if (!((String) from).isEmpty()) list.addAll(Arrays.asList(((String)from).split(",")));
				}
				return list;
			}

			@Override
			public Object set(List<String> t) {
				return t;
			}
		};
		
	}
	
	/***
	 * Create new string parser
	 * @param key the key
	 * @return the created configuration key
	 */
	public static QueryConfigurationKey<String> newString(String key) {
		return new QueryConfigurationKey<String>() {

			@Override
			public String name() {
				return key;
			}

			@Override
			public String get(Object from) {
				return from == null ? "" : from.toString();
			}

			@Override
			public Object set(String t) {
				return t;
			}
		};
		
	}
	
	/***
	 * Create new number parser
	 * @param key the key
	 * @return the created configuration key
	 */
	public static QueryConfigurationKey<Number> newNumber(String key) {
		return new QueryConfigurationKey<Number>() {

			@Override
			public String name() {
				return key;
			}

			@Override
			public Number get(Object from) {
				if (from instanceof String) {
					try {
						return Double.parseDouble((String)from);
					} catch (Throwable t) {
					}
				}
				return from instanceof Number ? (Number)from : 0;
			}

			@Override
			public Object set(Number t) {
				return t;
			}
			
		};
	}
	
}
