import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MultipleDumpTest {
	
	public static void main(String[] args) throws Exception {
		System.out.println("? MultipleDumpTest.main()");
		invokeMain("Class1",args);
		invokeMain("Class2",args);
		invokeMain("Class3",args);
	}

	private static void invokeMain (String className, String[] args) throws Exception
	{
		Class clazz = Class.forName(className);
		Class[] paramTypes = new Class[1];
		paramTypes[0] = args.getClass();
	
		Method method = clazz.getDeclaredMethod("main",paramTypes);
		Object[] params = new Object[1];
		params[0] = args;
		method.invoke(null,params);
	}
}