package caching;

import java.util.HashMap;
import java.util.Map;
  
public  aspect Caching {
	private Map<Integer,Integer> cache = new HashMap<Integer,Integer>();

	Integer around(Integer a): execution(* Fib.calc*(*)) && args(a) {
		if(cache.containsKey(a)){
			System.out.println("Using cached value for: " + a);
			return cache.get(a);
		}
		else {
			Integer result = proceed(a);
			cache.put(a, result);
			return result;
		}
	}
}
