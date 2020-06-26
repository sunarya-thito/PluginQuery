package septogeddon.pluginquery.library.remote;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReferenceHandler {

	private long id;
	private RemoteObject<?> object;
	private Map<Method,Long> cachedMethodLookup = new ConcurrentHashMap<>(); 
	
	public ReferenceHandler(long id, RemoteObject<?> object) {
		this.id = id;
		this.object = object;
	}
	
	public Map<Method, Long> getCachedMethodLookup() {
		return cachedMethodLookup;
	}
	
	public long getId() {
		return id;
	}
	
	public RemoteObject<?> getOwner() {
		return object;
	}
	
}
