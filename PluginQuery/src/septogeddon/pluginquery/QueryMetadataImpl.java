package septogeddon.pluginquery;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import septogeddon.pluginquery.api.QueryMetadata;
import septogeddon.pluginquery.api.QueryMetadataKey;
import septogeddon.pluginquery.utils.QueryUtil;

public class QueryMetadataImpl implements QueryMetadata {

	private Set<QueryMetadata> parents = ConcurrentHashMap.newKeySet();
	private Map<String, Set<Object>> map = new ConcurrentHashMap<>();
	@Override
	public <T> T getData(QueryMetadataKey<T> key) {
		QueryUtil.nonNull(key, "key");
		Set<Object> object = map.get(key.name());
		if (object == null) {
			for (QueryMetadata parent : parents) {
				T value = parent.getData(key, null);
				if (value != null) {
					return value;
				}
			}
			return null;
		}
		for (Object test : object) {
			if (key.isInstance(test)) {
				return key.cast(test);
			}
		}
		return null;
	}

	@Override
	public <T> void setData(QueryMetadataKey<T> key, T value) {
		QueryUtil.nonNull(key, "key");
		if (value == null) {
			map.remove(key.name());
			return;
		}
		map.computeIfAbsent(key.name(), name->ConcurrentHashMap.newKeySet()).add(value);
	}

	@Override
	public <T> T getData(QueryMetadataKey<T> key, T defaultValue) {
		QueryUtil.nonNull(key, "key");
		Set<Object> object = map.get(key.name());
		if (object == null) {
			for (QueryMetadata parent : parents) {
				T value = parent.getData(key, null);
				if (value != null) {
					return value;
				}
			}
			return defaultValue;
		}
		for (Object test : object) {
			if (key.isInstance(test)) {
				return key.cast(test);
			}
		}
		return defaultValue;
	}

	@Override
	public void addParent(QueryMetadata metadata) {
		QueryUtil.nonNull(metadata, "metadata");
		parents.add(metadata);
	}

	@Override
	public void removeParent(QueryMetadata metadata) {
		QueryUtil.nonNull(metadata, "metadata");
		parents.remove(metadata);
	}

	@Override
	public <T> boolean containsData(QueryMetadataKey<T> key) {
		return map.containsKey(key.name());
	}

}
