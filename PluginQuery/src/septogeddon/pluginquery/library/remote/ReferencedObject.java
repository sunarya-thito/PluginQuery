package septogeddon.pluginquery.library.remote;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ReferencedObject implements Externalizable {

	private long id;
	private AtomicLong lastCacheId;
	private Map<Long, Method> cachedMethodLookup;
	private Object object;
	
	public ReferencedObject(long id,Object object) {
		this.object = object;
		this.id = id;
		cachedMethodLookup = new ConcurrentHashMap<>();
		lastCacheId = new AtomicLong();
	}
	
	public long getId() {
		return id;
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
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(id);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		id = in.readLong();
	}
	
}
