package septogeddon.pluginquery.library.remote;

import java.util.HashMap;
import java.util.Map;

import septogeddon.pluginquery.utils.QueryUtil;

/***
 * Set of serializable classes and representators
 * @author Thito Yalasatria Sunarya
 *
 */
public class ClassRegistry {
	
	private Map<String, Class<?>> representators = new HashMap<>();
	
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
