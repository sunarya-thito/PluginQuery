package septogeddon.pluginquery.library.remote;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

/***
 * Context containing object references and prevent object from GarbageCollector
 * @author Thito Yalasatria Sunarya
 *
 */
public class ReferenceContext {

	private ArrayList<ReferencedObject> referenced = new ArrayList<>();
	private AtomicLong lastId = new AtomicLong();
	
	/***
	 * Clear all references
	 */
	public void clearReferences() {
		referenced.clear();
		lastId.set(0);
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
