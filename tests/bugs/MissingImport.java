import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;
import java.lang.reflect.Method;
//import java.lang.reflect.InvocationTargetException; <- crash with this line commented out

public aspect MissingImport {
	Object around() : 
			call(* *(..)) &&  !within(ImposterProcessing+) {
		MethodSignature sig = (MethodSignature)thisJoinPoint.getSignature();
		try {
			Method meth = ImposterProcessing.class.getMethod("dynamicThrow",  new Class[] { Throwable.class });
				 meth.invoke(this, new Object[] { null });
			} catch (InvocationTargetException e) {  // expect CE
				 throw new RuntimeException("framework error in throwing test exception ", e);
			} catch (IllegalAccessException e) {
				 throw new RuntimeException("framework error in throwing test exception (IllegalAccess)");
			}
		return null;
	}
}

class ImposterProcessing { }