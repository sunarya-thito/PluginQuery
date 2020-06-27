package septogeddon.pluginquery.library.remote;

import java.lang.reflect.Method;

/***
 * A proxy object generated at runtime for non-serializable objects
 * @author Thito Yalasatria Sunarya
 *
 */
public interface ObjectReference {

	/***
	 * Reflection Method for {@link #getReferenceHandler()}
	 */
	public static Method METHOD_GETREFERENCEHANDLER = ObjectReference.class.getDeclaredMethods()[0];
	/***
	 * Get the reference handler
	 * @return the handler
	 */
	public ReferenceHandler getReferenceHandler();
	
}
