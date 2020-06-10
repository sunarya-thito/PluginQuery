package septogeddon.pluginquery;

import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryPipeline;
import septogeddon.pluginquery.utils.QueryUtil;

public class QueryPipelineImpl implements QueryPipeline {

	private QueryChannelHandler root;
	
	public QueryChannelHandler findPosition(String name) {
		QueryChannelHandler now = root;
		while (now != null && !now.getName().equals(name)) {
			now = now.child;
		}
		return now != null && !now.getName().equals(name) ? null : now;
	}

	@Override
	public boolean remove(String str) {
		QueryChannelHandler current = findPosition(str);
		if (current == null) return false;
		if (current == root) {
			root = current.child;
		} else {
			current.parent.child = current.child;
		}
		try {
			current.onRemoved(this);
		} catch (Exception e) {
			dispatchUncaughtException(null, e);
		}
		return true;
	}

	@Override
	public boolean addFirst(QueryChannelHandler handler) {
		if (checkExistance(handler)) return false;
		if (root != null) root.child = handler;
		root = handler;
		handler.parent = root;
		try {
			handler.onAdded(this);
		} catch (Exception e) {
			dispatchUncaughtException(null, e);
		}
		return true;
	}

	@Override
	public boolean addBefore(String before, QueryChannelHandler handler) {
		if (checkExistance(handler)) return false;
		QueryChannelHandler current = findPosition(before);
		if (current == null) return false;
		if (current.parent == null) {
			root = handler;
		} else {
			current.parent.child = handler;
		}
		handler.parent = current.parent;
		handler.child = current;
		current.parent = handler;
		try {
			handler.onAdded(this);
		} catch (Exception e) {
			dispatchUncaughtException(null, e);
		}
		return true;
	}

	@Override
	public boolean addAfter(String after, QueryChannelHandler handler) {
		if (checkExistance(handler)) return false;
		QueryChannelHandler current = findPosition(after);
		if (current == null) return false;
		current.child.parent = handler;
		handler.child = current.child;
		handler.parent = current;
		current.child = handler;
		try {
			handler.onAdded(this);
		} catch (Exception e) {
			dispatchUncaughtException(null, e);
		}
		return true;
	}

	@Override
	public boolean addLast(QueryChannelHandler handler) {
		if (checkExistance(handler)) return false;
		if (root == null) {
			root = handler;
		} else {
			QueryChannelHandler now = root;
			while (now.child != null) {
				now = now.child;
			}
			now.child = handler;
			handler.parent = now;
		}
		try {
			handler.onAdded(this);
		} catch (Exception e) {
			dispatchUncaughtException(null, e);
		}
		return true;
	}
	
	public boolean checkExistance(QueryChannelHandler handler) {
		return get(handler.getName()) != null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends QueryChannelHandler> T get(String key) {
		return (T)findPosition(key);
	}
	
	@Override
	public void dispatchActive(QueryConnection connection) {
		if (root != null) {
			try {
				root.onActive(connection);
			} catch (Exception e) {
				dispatchUncaughtException(connection, e);
			}
		}
	}

	@Override
	public void dispatchInactive(QueryConnection connection) {
		if (root != null) {
			try {
				root.onInactive(connection);
			} catch (Exception e) {
				dispatchUncaughtException(connection, e);
			}
		}
	}

	@Override
	public byte[] dispatchSending(QueryConnection connection, byte[] bytes) {
		if (root != null) {
			try {
				bytes = root.onSending(connection, bytes);
				QueryUtil.nonNull(bytes, "bytes");
			} catch (Exception e) {
				dispatchUncaughtException(connection, e);
			}
		}
		return bytes;
	}

	@Override
	public byte[] dispatchReceiving(QueryConnection connection, byte[] bytes) {
		if (root != null) {
			try {
				bytes = root.onReceiving(connection, bytes);
				QueryUtil.nonNull(bytes, "bytes");
			} catch (Exception e) {
				dispatchUncaughtException(connection, e);
			}
		}
		return bytes;
	}

	@Override
	public void dispatchUncaughtException(QueryConnection connection, Throwable thrown) {
		if (root != null) {
			try {
				root.onUncaughtException(connection, thrown);
			} catch (Throwable e) {
				QueryUtil.Throw(e);
			}
		}
	}

}
