import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;
import java.lang.reflect.Method;

public aspect NewVoid {
	Object around() : 
			call(new(..))  {
		return proceed();
	}

    
	Object around() : 
			 call(* *(..)) {
		MethodSignature sig = (MethodSignature)thisJoinPoint.getSignature();
		Class returnType = sig.getReturnType();
		if (returnType == java.lang.Void.TYPE) {
			return new java.lang.Void(); // expect CE here
		} else {
			String s = "hi";
			Xyz xyz = null;   // expect CE here
			int x = s.count;  // expect CE here
			return proceed();
		}
	}
}
privileged aspect PrivCheck {    
	Object around() :  call(* *(..)) {
		Xyz xyz = null;         // expect CE here
		Object o = new Void(); // expect warning here
		int x = "goo".count; // expect warning here
		return null;
	}
}