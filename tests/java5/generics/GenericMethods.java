import java.util.*;
/*
 * test case fodder for basic generic signature matching
 */
public class GenericMethods {
	
	public List<Integer> returningListOfInteger() { 
		return new LinkedList<Integer>(); 
	}
	
	public List<Object> returningListOfObject() {
		return new LinkedList<Object>();
	}
	
	public List returningRawList() { return new ArrayList(); }
	
	public LinkedList<Integer> returningSubtypeOfListOfInteger() {
		return new LinkedList<Integer>();
	}
	
	public void takesAMap(Map<Double,Short> aMap) {}
	
	public void takesAHashmap(HashMap<Double,Short> aMap) {}
	
	public static void staticTakesAMap(Map<Double,Short> aMap) {}
	
	public void collectionOfAnything(Collection<?> aCollection) {}
	
	public void collectionOfAnyNumber(Collection<? extends Number> aNumberCollection) {}
	
	public void collectionOfAnythingTakingADouble(Collection<? super Double> aDoubleHandlingCollection) {}
	
	// now some fun with statics
	static <T> T findMax(List<T> ts) { return ts.get(0); }
	
	static <T extends Comparable<T>> T betterMax(Collection<T> collection) {
		return null;
	}
	
	static <T extends Comparable<? super T>> T evenBetterMax(Collection<T> coll) {
		return null;
	}
	
	static <T extends Object & Comparable<? super T>> T jdkMax(Collection<? extends T> coll) {
		return null;
	}
	
	static <T> void copy(List<T> dest, List<? extends T> src) {}
	
	static <T,S extends T> copyv2(List<T> dest, List<S> src) {}
}