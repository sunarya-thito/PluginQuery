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

/***
 * Strong reference for both sender and receiver. Shareable between both.
 * @author Thito Yalasatria Sunarya
 *
 */
public class ReferencedObject implements Externalizable {

	private static final long serialVersionUID = 1L;
	private long id;
	private AtomicLong lastCacheId;
	private Map<Long, Method> cachedMethodLookup;
	private Object object;
	private TypeHint hint;
	private boolean receiverSide;
	public ReferencedObject() {}
	public ReferencedObject(long id, TypeHint hint, boolean receiverSide) {
		this.id = id;
		this.hint = hint;
		this.receiverSide = receiverSide;
	}
	public ReferencedObject(long id,Object object, TypeHint hint) {
		this.object = object;
		this.hint = hint;
		this.id = id;
		cachedMethodLookup = new ConcurrentHashMap<>();
		lastCacheId = new AtomicLong();
	}
	
	public TypeHint getHintType() {
		return hint;
	}
	
	public boolean isReceiverSide() {
		return receiverSide;
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
		out.writeObject(hint);
		out.writeBoolean(receiverSide);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		id = in.readLong();
		hint = (TypeHint)in.readObject();
		receiverSide = in.readBoolean();
	}
	
}
