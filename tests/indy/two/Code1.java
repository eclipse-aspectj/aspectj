import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class Code1 {

	// Called via invokedynamic from a generated class
	private static void foo() {
		System.out.println("foo() is running");
	}

	public static CallSite bootstrap(MethodHandles.Lookup caller, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		Class<?> thisClass = lookup.lookupClass();
		MethodHandle mh = lookup.findStatic(thisClass, name, type);
		return new ConstantCallSite(mh);//mh.asType(type));
	}

}