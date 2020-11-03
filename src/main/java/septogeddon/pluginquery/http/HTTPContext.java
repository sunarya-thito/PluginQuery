package septogeddon.pluginquery.http;

import java.util.*;

public class HTTPContext {
    private static HTTPContext context = new HTTPContext();

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

    public void registerMethod(ProtocolMethod method) {
        protocolMethods.add(method);
    }

    public ProtocolMethod getMethod(String name) {
        for (ProtocolMethod method : protocolMethods) {
            if (method.name().equals(name)) {
                return method;
            }
        }
        return null;
    }

    public void registerListener(ListenerPriority priority, ProtocolListener listener) {
        listeners.computeIfAbsent(priority, x -> new LinkedHashSet<>()).add(listener);
    }

    public void unregisterListener(ProtocolListener listener) {
        listeners.values().removeIf(x -> x == listener);
    }

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
