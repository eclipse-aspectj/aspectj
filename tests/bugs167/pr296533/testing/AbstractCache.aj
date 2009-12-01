package testing;

import java.util.HashMap;
import java.util.Map;

public abstract aspect AbstractCache<Key,Value> {

	public abstract pointcut cachePoint(Key key);

	private Map<Object,Object> cache = new HashMap<Object,Object>();
	private Integer hitCount = 0;
	private Integer missCount = 0;
	
	Value around(Key key) : cachePoint(key){
		Value value = get(key);
		if(value == null){
			value = proceed(key);
			put(key,value);
			missCount++;
		} else {
			hitCount++;
		}
		return value;
	}

	@SuppressWarnings("unchecked")
	private Value get(Key key){
		return (Value) cache.get(key);
	}
	
	private void put(Key key, Value value) {
		cache.put(key, value);
	}

	public Integer getHitCount() {
		return hitCount;
	}

	public Integer getMissCount() {
		return missCount;
	}
	
}
