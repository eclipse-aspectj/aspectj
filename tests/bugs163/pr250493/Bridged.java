import java.lang.reflect.Method;
import java.util.*;

interface Super<T> {
	public T getterA();
}

public class Bridged implements Super<String> {
	public String getterA() {
		return "";
	}

	// Print BRIDGE status of all getter* methods
	public static void main(String[] argv) {
		Method[] ms = Bridged.class.getMethods();
		List results = new ArrayList(); 
		for (int i = 0; i < ms.length; i++) {
			if (ms[i].getName().startsWith("getter")) {
				results.add(ms[i].getName()+"()"+ms[i].getReturnType().getName()+ "  isBridged?"+((ms[i].getModifiers() & 0x0040) != 0));
			}
		}
		Collections.sort(results);
		for (Iterator iterator = results.iterator(); iterator.hasNext();) {
			String entry = (String) iterator.next();
			System.out.println(entry);
		}
	}
}

aspect X {
  public T Super<T>.getterB() { return null; }
}

