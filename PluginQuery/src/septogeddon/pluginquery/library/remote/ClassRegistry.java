package septogeddon.pluginquery.library.remote;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import septogeddon.pluginquery.utils.QueryUtil;

/***
 * Set of serializable classes and representators
 * @author Thito Yalasatria Sunarya
 *
 */
public class ClassRegistry {

	public static final ClassRegistry GLOBAL_REGISTRY = new ClassRegistry();
	private Map<String, Class<?>> representators = new HashMap<>();
	private Set<Class<?>> serializeInclusion = new HashSet<>();
	
	/***
	 * Initialize the registry with default serialize inclusion: {@link java.lang.String}, {@link java.lang.Boolean}, {@link java.lang.Character}, {@link java.lang.Number}.
	 */
	public ClassRegistry() {
		addInclusion(String.class);
		addInclusion(Boolean.class);
		addInclusion(Character.class);
		addInclusion(Number.class);
	}
	
	/***
	 * Add a class into serialize inclusion
	 * @param cl the class
	 */
	public void addInclusion(Class<?> cl) {
		/*
		 * code deletion reason:
		 * See line 23 (addInclusion(Number.class))
		 * its not serializable nor externalizable, but however, its a superclass of integer, double, float, etc.
		 * So, basically, add inclusion allows anyclass (superclass or the class it self).
		 */
//		QueryUtil.illegalArgument(!cl.isAssignableFrom(Serializable.class) && !cl.isAssignableFrom(Externalizable.class), "not serializable");
		serializeInclusion.add(cl);
	}
	
	/***
	 * Remove a class from serialize inclusion
	 * @param cl the class
	 */
	public void removeInclusion(Class<?> cl) {
		serializeInclusion.remove(cl);
	}
	
	/***
	 * Check if the class is serializable
	 * @param cl the class
	 * @return true if its serializable
	 */
	public boolean isSerializable(Class<?> cl) {
		for (Class<?> clx : serializeInclusion) {
			if (clx.isAssignableFrom(cl)) {
				return true;
			}
		}
		return false;
	}
	
	/***
	 * Get class from name
	 * @param name the class name
	 * @return the class
	 * @throws ClassNotFoundException if there is no such class or no representator available
	 */
	public Class<?> getClass(String name) throws ClassNotFoundException {
		return representators.getOrDefault(name, Class.forName(name));
	}
	
	/***
	 * Register a representator class
	 * @param cl interface class
	 */
	public void registerClass(Class<?> cl) {
		QueryUtil.illegalArgument(!cl.isInterface(), "must be an interface");
		Substitute rpc = cl.getAnnotation(Substitute.class);
		if (rpc != null) {
			for (Class<?> clx : rpc.value()) {
				representators.put(clx.getName(), cl);
			}
		}
		Represent rpu = cl.getAnnotation(Represent.class);
		if (rpu != null) {
			for (String clx : rpu.value()) {
				representators.put(clx, cl);
			}
		}
	}
	
	/***
	 * Unregister a representator class
	 * @param cl interface class
	 */
	public void unregisterClass(Class<?> cl) {
		QueryUtil.illegalArgument(!cl.isInterface(), "must be an interface");
		Substitute rpc = cl.getAnnotation(Substitute.class);
		if (rpc != null) {
			for (Class<?> clx : rpc.value()) {
				representators.remove(clx.getName(), cl);
			}
		}
		Represent rpu = cl.getAnnotation(Represent.class);
		if (rpu != null) {
			for (String clx : rpu.value()) {
				representators.remove(clx, cl);
			}
		}
	}
	
}
