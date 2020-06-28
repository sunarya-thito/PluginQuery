package septogeddon.pluginquery.library.remote;

import java.util.ArrayList;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;

/***
 * Context containing object references and prevent object from GarbageCollector
 * @author Thito Yalasatria Sunarya
 *
 */
public class ReferenceContext {

	private WeakHashMap<Long, ObjectReference> references = new WeakHashMap<>();
	private ArrayList<ReferencedObject> referenced = new ArrayList<>();
	private AtomicLong lastId = new AtomicLong();
	
	/***
	 * Clear all references
	 */
	public void clearReferences() {
		references.clear();
		referenced.clear();
		lastId.set(0);
	}
	
	/***
	 * Get existing reference saved on this Remote side
	 * @param id the id of reference object
	 * @return the reference object
	 */
	public ObjectReference getExistingReference(long id) {
		return references.get(id);
	}
	
	/***
	 * Put existing reference into this Remote side
	 * @param reference the reference object
	 */
	public void putExistingReference(ObjectReference reference) {
		references.put(reference.getReferenceHandler().getId(), reference);
	}
	
	/***
	 * Create or get existing reference
	 * @param object the object instance
	 * @return a referenced object
	 */
	public ReferencedObject createReference(TypeHint hint, Object object) {
		for (ReferencedObject reference : referenced) {
			if (reference.getObject() == object) return reference;
		}
		long id = lastId.getAndIncrement();
		ReferencedObject reference = new ReferencedObject(id, object, hint);
		referenced.add(reference);
		return reference;
	}
	 
	/***
	 * Close reference
	 * @param id reference id
	 */
	public void closeReference(long id) {
		for (int i = referenced.size()-1; i >= 0; i--) {
			ReferencedObject reference = referenced.get(i);
			if (reference.getId() == id) {
				referenced.remove(i);
				break;
			}
		}
	}
	
	/***
	 * Get existing reference
	 * @param id reference id
	 * @return the referenced object
	 */
	public ReferencedObject getReferenced(long id) {
		for (int i = 0; i < referenced.size(); i++) {
			ReferencedObject ref = referenced.get(i);
			if (ref.getId() == id) {
				return ref;
			}
		}
		throw new IllegalStateException("no reference");
	}
	
}
