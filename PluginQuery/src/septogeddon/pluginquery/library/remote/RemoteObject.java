package septogeddon.pluginquery.library.remote;

import java.io.Externalizable;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryListener;
import septogeddon.pluginquery.utils.InstanceBuffer;
import septogeddon.pluginquery.utils.QueryUtil;

public class RemoteObject<T> {

	private static Method finalizeMethod;
	static {
		try {
			finalizeMethod = Object.class.getDeclaredMethod("finalize");
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("cannot find finalize method on Object. Bug?", e);
		}
	}
	protected ExecutorService executorService;
	protected String channel;
	protected long futureTimeout = 1000 * 30;
	protected T object;
	protected QueryConnection connection;
	protected AtomicLong lastId = new AtomicLong();
	protected boolean queueQuery = true;
	protected AtomicLong lastQueueId = new AtomicLong();
	
	protected Map<Long, WeakReferencedObject> referenced = new ConcurrentHashMap<>();
	protected Map<Long, RemoteFuture> queuedInvocation = new ConcurrentHashMap<>();
	
	protected RemoteListener listener = new RemoteListener();
	
	protected boolean closed;
	protected T crossoverObject;
	public RemoteObject(String channel, QueryConnection connection, Class<T> clazz) {
		this(channel, connection, null, clazz);
	}
	@SuppressWarnings("unchecked")
	public RemoteObject(String channel, QueryConnection connection, T object) {
		this(channel, connection, object, (Class<T>)object.getClass());
	}
	public RemoteObject(String channel, QueryConnection connection, T object, Class<T> clazz) {
		QueryUtil.illegalState(!clazz.isInterface(), "represented remote object class must be an interface");
		this.channel = channel;
		this.object = object;
		this.connection = connection;
		QueryUtil.illegalState(createReference(object) != 0, "unstatisfied owned object id");
		crossoverObject = newObject(0, clazz);
		connection.getEventBus().registerListener(listener);
	}
	
	public boolean isQueueQuery() {
		return queueQuery;
	}
	
	public void setQueueQuery(boolean queueQuery) {
		this.queueQuery = queueQuery;
	}
	
	public ExecutorService getExecutorService() {
		return executorService;
	}
	
	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}
	
	public long getFutureTimeout() {
		return futureTimeout;
	}
	
	public void setFutureTimeout(long futureTimeout) {
		this.futureTimeout = futureTimeout;
	}
	
	public void close() {
		closed = true;
		referenced.clear();
		queuedInvocation.forEach((a,b)->b.completeExceptionally(new IllegalStateException("remote connection closed")));
		queuedInvocation.clear();
		connection.getEventBus().unregisterListener(listener);
	}
	
	public boolean isClosed() {
		return closed;
	}
	
	protected long nextQueueId() {
		return lastQueueId.getAndIncrement();
	}
	
	protected long nextId() {
		return lastId.getAndIncrement();
	}
	
	public T getCrossoverObject() {
		return crossoverObject;
	}
	
	public QueryConnection getConnection() {
		return connection;
	}
	
	public T getObject() {
		return object;
	}
	
	protected void submit(Runnable r) {
		if (executorService == null) r.run(); // synchronous anyway
		else executorService.submit(r); // asynchronous
	}
	
	protected Method findMethod(Object refer, String method, Class<?>[] hint, Object...args) {
		for (Method met : refer.getClass().getMethods()) {
			if (met.getParameterCount() == args.length) {
				boolean match = true;
				Class<?>[] param = met.getParameterTypes();
				for (int i = 0; i < args.length; i++) {
					Class<?> clazz = param[i];
					Object object = args[i];
					Class<?> givenHint = hint[i];
					if (givenHint != null && !clazz.isAssignableFrom(givenHint)) {
						match = false;
						break;
					}
					if (object != null && !clazz.isInstance(object)) {
						match = false;
						break;
					}
				}
				if (match) {
					return met;
				}
			}
		}
		return null;
	}
	
	protected long createReference(Object object) {
		for (Entry<Long, WeakReferencedObject> reference : referenced.entrySet()) {
			if (reference.getValue().getObject() == object) return reference.getKey();
		}
		long id = nextId();
		referenced.put(id, new WeakReferencedObject(object));
		return id;
	}
 	
	protected Object[] filter(Object[] args) {
		for (int i = 0; i < args.length; i++) {
			args[i] = filter(args[i]);
		}
		return args;
	}
	
	protected Object filter(Object target) { 
		if (!(target instanceof Serializable) && !(target instanceof Externalizable) && target != null) {
			return new CoveredObject(createReference(target));
		}
		return target;
	}
	
	protected void closeReference(long id) {
		referenced.remove(id);
	}
	
	protected void closeCrossReference(long id) {
		InstanceBuffer buffer = new InstanceBuffer();
		buffer.pushObject(RemoteContext.COMMAND_CLOSE_REFERENCE);
		buffer.pushObject(id);
		try {
			connection.sendQuery(channel, buffer.toByteArray(), isQueueQuery());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("all")
	protected <K> K newObject(long id, Class<K> interf) {
		final ReferenceHandler loader = new ReferenceHandler(id, this);
		return (K)
				Proxy.newProxyInstance(
						interf.getClassLoader(), 
						new Class<?>[] {ObjectReference.class, interf}, 
				(proxy, method, args)->{
					if (method.equals(finalizeMethod)) {
						closeCrossReference(loader.getId());
						return null;
					}
					if (closed) throw new IllegalStateException("remote object closed");
					if (method.equals(ObjectReference.METHOD_GETOBJECTLOADER)) {
						return loader;
					}
					InstanceBuffer buffer = new InstanceBuffer();
					buffer.pushObject(RemoteContext.COMMAND_INVOKE_METHOD);
					long queueId = nextQueueId();
					RemoteFuture future = new RemoteFuture(loader);
					queuedInvocation.put(queueId, future);
					buffer.pushObject(queueId);
					Long methodId = loader.getCachedMethodLookup().get(method);
					buffer.pushObject(loader.getId());
					buffer.pushObject(methodId);
					buffer.pushObject(method.getName());
					buffer.pushObject(filter(args));
					if (methodId == null) {
						Class<?>[] param = method.getParameterTypes();
						String[] parameters = new String[method.getParameterCount()];
						for (int i = 0; i < parameters.length; i++) {
							if (param[i] != null) {
								parameters[i] = param[i].getName();
							}
						}
						buffer.pushObject(parameters);
					}
					connection.sendQuery(channel, buffer.toByteArray(), isQueueQuery())
						.addListener(future);
					return futureTimeout < 0 ? future.get() : future.get(futureTimeout, TimeUnit.MILLISECONDS);
				});
	}
	
	class RemoteListener implements QueryListener {

		@Override
		public void onConnectionStateChange(QueryConnection connection) {
		}

		@Override
		public void onQueryReceived(QueryConnection connection, String channel, byte[] message) throws ClassNotFoundException, IOException {
			if (RemoteObject.this.channel.equals(channel)) {
				InstanceBuffer buffer = new InstanceBuffer(message);
				byte command = buffer.pullObject();
				if (command == RemoteContext.COMMAND_INVOKE_METHOD) {
					long queueId = buffer.pullObject();
					long objectId = buffer.pullObject();
					Long methodId = buffer.pullObject();
					String methodName = buffer.pullObject();
					try {
						WeakReferencedObject stored = referenced.get(objectId);
						if (stored == null) {
							throw new NullPointerException("no such object id "+objectId);
						}
						Object[] arguments = buffer.pullObject();
						Method method;
						if (methodId == null) {
							String[] paramName = buffer.pullObject();
							Class<?>[] parameters = new Class<?>[paramName.length];
							for (int i = 0; i < parameters.length; i++) {
								if (paramName[i] != null) {
									try {
										parameters[i] = Class.forName(paramName[i]);
									} catch (ClassNotFoundException e) {
									}
								}
							}
							method = findMethod(stored.getObject(), methodName, parameters, arguments);
						} else {
							method = stored.getCachedMethodLookup().get(methodId);
						}
						if (method != null) {
							final Method finalMethod = method;
							submit(()->{
								try {
									Object result = finalMethod.invoke(stored.getObject(), arguments);
									buffer.finalize();
									buffer.pushObject(RemoteContext.COMMAND_RETURN_METHOD);
									buffer.pushObject(queueId);
									if (methodId == null) {
										buffer.pushObject(stored.cacheMethod(finalMethod));
									} else {
										buffer.pushObject(methodId);
									}
									buffer.pushObject(result);
									connection.sendQuery(channel, buffer.toByteArray(), isQueueQuery());
								} catch (IllegalAccessException e) {
								} catch (IllegalArgumentException | IOException e) {
									e.printStackTrace();
								} catch (InvocationTargetException e) {
									buffer.finalize();
									buffer.pushObject(RemoteContext.COMMAND_DELIVERED_EXCEPTION);
									buffer.pushObject(queueId);
									buffer.pushObject(e);
									try {
										connection.sendQuery(channel, buffer.toByteArray(), isQueueQuery());
									} catch (IOException e1) {
										e1.printStackTrace();
									}
								}
							});
						} else throw new NoSuchMethodException(methodName);
					} catch (Throwable t) {
						buffer.finalize();
						buffer.pushObject(RemoteContext.COMMAND_DELIVERED_EXCEPTION);
						buffer.pushObject(queueId);
						buffer.pushObject(t);
						connection.sendQuery(channel, buffer.toByteArray(), isQueueQuery());
					}
					return;
				}
				if (command == RemoteContext.COMMAND_CLOSE_REFERENCE) {
					long objectId = buffer.pullObject();
					closeReference(objectId);
					return;
				}
				if (command == RemoteContext.COMMAND_RETURN_METHOD) {
					long queueId = buffer.pullObject();
					long methodId = buffer.pullObject();
					RemoteFuture future = queuedInvocation.remove(queueId);
					if (future == null) {
						throw new IllegalStateException("no such future");
					} else {
						if (future instanceof RemoteMethodInvocationFuture) {
							future.getHandler().getCachedMethodLookup().put(((RemoteMethodInvocationFuture) future).getMethod(), methodId);
						}
						Object object = buffer.pullObject();
						if (object instanceof CoveredObject) {
							CoveredObject covered = (CoveredObject)object;
							object = newObject(covered.getId(), future.getHint());
						}
						future.complete(object);
					}
					return;
				}
				if (command == RemoteContext.COMMAND_DELIVERED_EXCEPTION) {
					Long queueId = buffer.pullObject();
					Throwable thrown = buffer.pullObject();
					if (queueId == null) {
						thrown.printStackTrace();
					} else {
						RemoteFuture future = queuedInvocation.remove(queueId);
						if (future == null) {
							thrown.printStackTrace();
						} else {
							future.completeExceptionally(thrown);
						}
					}
					return;
				}
			}
		}
	}
	
}
