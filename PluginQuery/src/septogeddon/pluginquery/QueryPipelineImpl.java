package septogeddon.pluginquery;

import java.util.ArrayList;
import java.util.Collection;

import septogeddon.pluginquery.api.QueryConnection;
import septogeddon.pluginquery.api.QueryPipeline;
import septogeddon.pluginquery.utils.QueryUtil;

public class QueryPipelineImpl implements QueryPipeline {

	private ArrayList<QueryChannelHandler> handlers = new ArrayList<>();
	public int findPos(String s) {
		for (int i = 0; i < handlers.size(); i++) {
			QueryChannelHandler hand = handlers.get(i);
			if (hand.getName().equals(s)) {
				return i;
			}
		}
		return -1;
	}
	@Override
	public boolean remove(String str) {
		return handlers.removeIf(handler->{
			if (handler.getName().equals(str)) {
				try {
					handler.onRemoved(this);
				} catch (Exception e) {
					dispatchUncaughtException(null, e);
				}
				return true;
			}
			return false;
		});
	}

	@Override
	public boolean addFirst(QueryChannelHandler handler) {
		remove(handler);
		handlers.add(0, handler);
		try {
			handler.onAdded(this);
		} catch (Exception e) {
			dispatchUncaughtException(null, e);
		}
		return true;
	}

	@Override
	public boolean addBefore(String before, QueryChannelHandler handler) {
		remove(handler);
		int index = findPos(before);
		if (index < 0) return false;
		handlers.add(index, handler);
		try {
			handler.onAdded(this);
		} catch (Exception e) {
			dispatchUncaughtException(null, e);
		}
		return true;
	}

	@Override
	public boolean addAfter(String after, QueryChannelHandler handler) {
		remove(handler);
		int index = findPos(after);
		if (index < 0) return false;
		handlers.add(index + 1, handler);
		try {
			handler.onAdded(this);
		} catch (Exception e) {
			dispatchUncaughtException(null, e);
		}
		return true;
	}

	@Override
	public boolean addLast(QueryChannelHandler handler) {
		remove(handler);
		handlers.add(handler);
		try {
			handler.onAdded(this);
		} catch (Exception e) {
			dispatchUncaughtException(null, e);
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends QueryChannelHandler> T get(String key) {
		int index = findPos(key);
		return index < 0 ? null : (T)handlers.get(index);
	}
	
	@Override
	public void dispatchActive(QueryConnection connection) {
		if (!handlers.isEmpty()) {
			try {
				handlers.get(0).onActive(connection);
			} catch (Exception e) {
				dispatchUncaughtException(connection, e);
			}
		}
	}

	@Override
	public void dispatchInactive(QueryConnection connection) {
		if (!handlers.isEmpty()) {
			try {
				handlers.get(0).onInactive(connection);
			} catch (Exception e) {
				dispatchUncaughtException(connection, e);
			}
		}
	}

	@Override
	public byte[] dispatchSending(QueryConnection connection, byte[] bytes) {
		if (!handlers.isEmpty()) {
			try {
				bytes = handlers.get(0).onSending(connection, bytes);
				QueryUtil.nonNull(bytes, "bytes");
			} catch (Exception e) {
				dispatchUncaughtException(connection, e);
			}
		}
		return bytes;
	}

	@Override
	public byte[] dispatchReceiving(QueryConnection connection, byte[] bytes) {
		if (!handlers.isEmpty()) {
			try {
				bytes = handlers.get(0).onReceiving(connection, bytes);
				QueryUtil.nonNull(bytes, "bytes");
			} catch (Exception e) {
				dispatchUncaughtException(connection, e);
			}
		}
		return bytes;
	}

	@Override
	public void dispatchUncaughtException(QueryConnection connection, Throwable thrown) {
		if (!handlers.isEmpty()) {
			try {
				handlers.get(0).onCaughtException(connection, thrown);
			} catch (Throwable e) {
				QueryUtil.Throw(e);
			}
		} else thrown.printStackTrace();
	}
	@Override
	public QueryChannelHandler nextHandler(QueryChannelHandler of) {
		int pos = handlers.indexOf(of);
		return pos + 1 >= handlers.size() ? null : handlers.get(pos + 1);
	}
	@Override
	public QueryChannelHandler first() {
		return handlers.isEmpty() ? null : handlers.get(0);
	}
	@Override
	public QueryChannelHandler last() {
		return handlers.isEmpty() ? null : handlers.get(handlers.size()-1);
	}
	@Override
	public Collection<? extends QueryChannelHandler> getPipes() {
		return new ArrayList<>(handlers);
	}

}
