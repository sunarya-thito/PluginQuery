package septogeddon.pluginquery.api;

public interface QueryMetadataKey<T> {

	public String name();
	public T deserialize(Object from);
	public Object serialize(T from);
	
	public static <T> QueryMetadataKey<T> newCastableKey(String key, Class<T> cl) {
		return new QueryMetadataKey<T>() {

			@Override
			public String name() {
				return key;
			}

			@Override
			public T deserialize(Object from) {
				return cl.isInstance(from) ? cl.cast(from) : null;
			}

			@Override
			public Object serialize(T from) {
				return from;
			}
			
		};
	}
	
}
