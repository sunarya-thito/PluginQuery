package septogeddon.pluginquery.api;

/***
 * Metadata Key used to get Metadata Value
 * @author Thito Yalasatria Sunarya
 * @see QueryMetadata
 *
 * @param <T> Anything
 */
public interface QueryMetadataKey<T> {

	/***
	 * Key name
	 * @return
	 */
	public String name();
	/***
	 * Handle object casting
	 * @param from Object stored in the metadata storage
	 * @return Casted object
	 */
	public T cast(Object from);
	/***
	 * Determine if the object is exactly what we wanted
	 * @param from Object stored in the metadata storage
	 * @return true if its the object we wanted
	 * @see #cast(Object)
	 */
	public boolean isInstance(Object from);
	
	/***
	 * Create new key using Cast-able rule where this key only accept value that instance of the specified class
	 * @param <T> Anything
	 * @param key The key name for {@link #name()}
	 * @param cl The class owner
	 * @return The created metadata key
	 */
	public static <T> QueryMetadataKey<T> newCastableKey(String key, Class<T> cl) {
		return new QueryMetadataKey<T>() {

			@Override
			public String name() {
				return key;
			}

			@Override
			public T cast(Object from) {
				return cl.cast(from);
			}

			@Override
			public boolean isInstance(Object from) {
				return cl.isInstance(from);
			}

		};
	}
	
}
