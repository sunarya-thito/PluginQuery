package septogeddon.pluginquery.library.remote;

import java.lang.reflect.Method;

public class RemoteMethodInvocationFuture extends RemoteFuture {

	private Method method;
	public RemoteMethodInvocationFuture(ReferenceHandler handler, Method method) {
		super(handler);
		this.method = method;
	}
	
	public Method getMethod() {
		return method;
	}

}
