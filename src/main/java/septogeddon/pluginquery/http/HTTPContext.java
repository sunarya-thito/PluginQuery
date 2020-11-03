package septogeddon.pluginquery.http;

import java.util.*;

/**
 * Basically a HTTP Protocol Manager for PluginQuery
 *
 * @author Thito Yalasatria Sunarya
 */
public class HTTPContext {
    private static HTTPContext context = new HTTPContext();

    /**
     * Get the default HTTP context
     * @return HTTPContext instance
     */
    public static HTTPContext getContext() {
        return context;
    }

    static {
        for (ProtocolMethod.General method : ProtocolMethod.General.values()) {
            getContext().registerMethod(method);
        }
        getContext().registerListener(ListenerPriority.WRITE, new DefaultProtocolListener());
    }

    private Set<ProtocolMethod> protocolMethods = new HashSet<>();
    private EnumMap<ListenerPriority, Set<ProtocolListener>> listeners = new EnumMap<ListenerPriority, Set<ProtocolListener>>(ListenerPriority.class);

    /**
     * Register a method
     * @param method method type
     * @throws IllegalArgumentException if the ProtocolMethod has different context
     * @see ProtocolMethod#getContext()
     */
    public void registerMethod(ProtocolMethod method) {
        if (method.getContext() != this) throw new IllegalArgumentException("different context");
        protocolMethods.add(method);
    }

    /**
     * Get method by name
     * @param name the method name
     * @return the method instance
     */
    public ProtocolMethod getMethod(String name) {
        for (ProtocolMethod method : protocolMethods) {
            if (method.name().equalsIgnoreCase(name)) {
                return method;
            }
        }
        return null;
    }

    /**
     * Register a listener
     * @param priority the listener priority
     * @param listener the listener
     */
    public void registerListener(ListenerPriority priority, ProtocolListener listener) {
        listeners.computeIfAbsent(priority, x -> new LinkedHashSet<>()).add(listener);
    }

    /**
     * Unregister a listener
     * @param listener the listener
     */
    public void unregisterListener(ProtocolListener listener) {
        listeners.values().removeIf(x -> x == listener);
    }

    /**
     * Dispatch the request to all listeners
     * @param request the request
     * @param client the client
     */
    public void dispatchRequest(ProtocolRequest request, ProtocolClient client) {
        for (Map.Entry<ListenerPriority, Set<ProtocolListener>> listener : listeners.entrySet()) {
            for (ProtocolListener val : listener.getValue()) {
                try {
                    val.onRequest(client, request);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                if (client.isStartWrite()) {
                    // a listener has just wrote something in the
                    if (!listener.getKey().allowWrite()) throw new IllegalStateException("disallowed write for listener "+val);
                    return;
                }
            }
        }
    }

}
