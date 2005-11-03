import java.util.*;

public class Test<T> {

	Set<T> set = new HashSet<T>();

	public <T> T[] toArray(T[] a) {
    System.err.println("In toArray()");
		return set.toArray(a);
	}
}
