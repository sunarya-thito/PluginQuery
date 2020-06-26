package septogeddon.pluginquery.library.remote;

import java.lang.reflect.Method;

public interface ObjectReference {

	public static Method METHOD_GETOBJECTLOADER = ObjectReference.class.getDeclaredMethods()[0];
	public ReferenceHandler getObjectLoader();
	
}
