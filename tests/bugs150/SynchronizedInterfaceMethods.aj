import java.lang.reflect.*;

public class SynchronizedInterfaceMethods  {
	
	public static void main(String[] args) throws NoSuchMethodException {
		Class myClass = SynchronizedInterfaceMethods.class;
		Method m = myClass.getMethod("foo");
		if (!Modifier.isSynchronized(m.getModifiers())) throw new RuntimeException("Expecting method on class to be synchronized");
		Class iClass = I.class;
		Method im = iClass.getMethod("foo");
		if (Modifier.isSynchronized(im.getModifiers())) throw new RuntimeException("Interface method must NOT be synchronized");
	}
	
	
}

interface I {}


aspect A {
	
	public synchronized void I.foo() {}
	
	declare parents : SynchronizedInterfaceMethods implements I;
}