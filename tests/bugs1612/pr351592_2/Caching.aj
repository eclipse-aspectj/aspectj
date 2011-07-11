package caching;

import java.util.HashMap;
import java.util.Map;
  
public abstract aspect Caching<K,V> {
	private Map<K,V> cache = new HashMap<K,V>();

	abstract pointcut cached();

	V around(K a): cached() && args(a) {
		if(cache.containsKey(a)){
			System.out.println("Using cached value for: " + a);
			return cache.get(a);
		}
		else {
			V result = proceed(a);
			cache.put(a, result);
			return result;
		}
	}
}
