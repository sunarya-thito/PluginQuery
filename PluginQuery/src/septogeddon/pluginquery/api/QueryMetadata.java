package septogeddon.pluginquery.api;

/***
 * Handle non-persistent metadata
 * @author Thito Yalasatria Sunarya
 *
 */
public interface QueryMetadata {
	
	/***
	 * Add parent for further lookup
	 * @param metadata
	 */
	public void addParent(QueryMetadata metadata);
	/***
	 * Remove parent
	 * @param metadata
	 */
	public void removeParent(QueryMetadata metadata);
	/***
	 * Get data from this storage and parent storage
	 * @param <T>
	 * @param key
	 * @return
	 */
	public <T> T getData(QueryMetadataKey<T> key);
	/***
	 * Get data from this storage and parent storage, will use defaultValue if its null
	 * @param <T>
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public <T> T getData(QueryMetadataKey<T> key, T defaultValue);
	/***
	 * Set data to current storage, will remove if its null, does not affect the parent metadata
	 * @param <T>
	 * @param key
	 * @param value
	 */
	public <T> void setData(QueryMetadataKey<T> key, T value);
	/***
	 * Check if this storage contains wanted data, does not check the parent 
	 * @param key
	 * @return
	 */
	public <T> boolean containsData(QueryMetadataKey<T> key);
	
}
