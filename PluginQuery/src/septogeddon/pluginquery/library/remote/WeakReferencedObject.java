package septogeddon.pluginquery.library.remote;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class WeakReferencedObject {

	private AtomicLong lastCacheId = new AtomicLong();
	private Map<Long, Method> cachedMethodLookup = new ConcurrentHashMap<>();
	private Object object;
	
	public WeakReferencedObject(Object object) {
		this.object = object;
	}
	
	public Map<Long, Method> getCachedMethodLookup() {
		return cachedMethodLookup;
	}
	
	public Object getObject() {
		return object;
	}
	
	public long cacheMethod(Method method) {
		for (Entry<Long,Method> met : cachedMethodLookup.entrySet()) {
			if (met.getValue().equals(method)) {
				return met.getKey();
			}
		}
		long id = lastCacheId.getAndIncrement();
		cachedMethodLookup.put(id, method);
		return id;
	}
	
}
