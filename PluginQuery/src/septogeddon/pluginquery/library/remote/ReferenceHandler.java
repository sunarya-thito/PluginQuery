package septogeddon.pluginquery.library.remote;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/***
 * The reference handler for {@link ObjectReference}
 * @author Thito Yalasatria Sunarya
 *
 */
public class ReferenceHandler {

	private long id;
	private RemoteObject<?> object;
	private Map<Method,Long> cachedMethodLookup = new ConcurrentHashMap<>(); 
	
	public ReferenceHandler(long id, RemoteObject<?> object) {
		this.id = id;
		this.object = object;
	}
	
	/***
	 * Get cached method
	 * @return map of cached method
	 */
	public Map<Method, Long> getCachedMethodLookup() {
		return cachedMethodLookup;
	}
	
	/***
	 * Get the reference id
	 * @return id
	 */
	public long getId() {
		return id;
	}
	
	/***
	 * Get the owner
	 * @return RemoteObject owner
	 */
	public RemoteObject<?> getOwner() {
		return object;
	}
	
}
