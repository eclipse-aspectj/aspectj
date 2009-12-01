package testing;

import java.lang.reflect.Method;

public class TestRunner {

	public static void main(String[] args) {
		ResourceManager manager = new ResourceManager();
		ResourceCache cache = ResourceCache.aspectOf();
		
		Resource r1_1 = manager.lookupResource("1");
		Resource r1_2 = manager.lookupResource("1");
		Resource r1_3 = manager.lookupResource("1");
		Resource r1_4 = manager.lookupResource("1");
		Resource r1_5 = manager.lookupResource("1");
		
		Resource r2_1 = manager.lookupResource("2");
		Resource r2_2 = manager.lookupResource("2");

		System.out.println("Cache hits: " + cache.getHitCount());
		System.out.println("Cache hits: " + cache.getMissCount());
	}

}
