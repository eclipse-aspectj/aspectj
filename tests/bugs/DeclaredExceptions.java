import java.lang.reflect.Method;
import java.io.*;
import org.aspectj.testing.Tester;

public class DeclaredExceptions {
	public static void main(String[] args) throws Exception {
		Class c = C.class;
		Method m = c.getDeclaredMethod("m", new Class[0]);
		Tester.checkEqual(m.getExceptionTypes().length, 1);
		Tester.checkEqual(m.getExceptionTypes()[0], IOException.class);
		
		c = I.class;
		m = c.getDeclaredMethod("m", new Class[0]);
		Tester.checkEqual(m.getExceptionTypes().length, 1);
		Tester.checkEqual(m.getExceptionTypes()[0], IOException.class);
	}
}

interface I {}

class C {}

aspect A {
	public void C.m() throws IOException {
	}
	
	public void I.m() throws IOException { }
}