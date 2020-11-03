package septogeddon.pluginquery.library.remote;

import java.lang.reflect.Method;

/**
 * {@link RemoteFuture} for Method
 * @author Thito Yalasatria Sunarya
 *
 */
public class RemoteMethodInvocationFuture extends RemoteFuture {

    private final Method method;
    private final ReferenceHandler handler;

    public RemoteMethodInvocationFuture(ReferenceHandler handler, Method method) {
        this.handler = handler;
        this.method = method;
    }

    /**
     * Get the reference handler
     * @return reference handler
     */
    public ReferenceHandler getHandler() {
        return handler;
    }

    /**
     * Get the method
     * @return method
     */
    public Method getMethod() {
        return method;
    }

}
