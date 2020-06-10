package septogeddon.pluginquery.api;

import java.util.ArrayList;
import java.util.List;

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
	
	public static QueryConfigurationKey<Boolean> newBoolean(String key) {
		return new QueryConfigurationKey<Boolean>() {

			@Override
			public String name() {
				return key;
			}

			@Override
			public Boolean get(Object from) {
				return from == Boolean.TRUE;
			}

			@Override
			public Object set(Boolean t) {
				return t;
			}
			
		};
	}
	
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
					list.add((String)from);
				}
				return list;
			}

			@Override
			public Object set(List<String> t) {
				return t;
			}
		};
		
	}
	
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
	
	public static QueryConfigurationKey<Number> newNumber(String key) {
		return new QueryConfigurationKey<Number>() {

			@Override
			public String name() {
				return key;
			}

			@Override
			public Number get(Object from) {
				return from instanceof Number ? (Number)from : 0;
			}

			@Override
			public Object set(Number t) {
				return t;
			}
			
		};
	}
	
}
