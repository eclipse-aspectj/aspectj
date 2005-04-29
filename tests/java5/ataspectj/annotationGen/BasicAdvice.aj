
import java.lang.reflect.Method;
import org.aspectj.lang.annotation.*;

public aspect BasicAdvice {
	
	@SuppressAjWarnings
	before() : execution(* *.*(..)) {
		//
	}
	
	@SuppressAjWarnings
	after() : call(* Integer.*(..)) {
		
	}
	
	@SuppressAjWarnings
	after() returning : get(String *) {
		
	}
	
	@SuppressAjWarnings
	after() returning(String strVal) : get(String *) {
		
	}
	
	@SuppressAjWarnings
	after() throwing : execution(* *.*(..)) {
		
	}
	
	@SuppressAjWarnings
	after() throwing(RuntimeException ex) : execution(* *.*(..)) {
		
	}
	
	@SuppressAjWarnings
	void around() : set(* foo) {
		proceed();
	}
	
	private static int adviceCount = 0;
	
	public static void main(String[] args) {
		Method[] methods = BasicAdvice.class.getDeclaredMethods();
		for (Method method : methods) {
			adviceCount++;
			if (method.getName().startsWith("ajc$before$")) {
				checkBefore(method);
			} else if (method.getName().startsWith("ajc$after$")) {
				checkAfter(method);
			} else if (method.getName().startsWith("ajc$afterReturning$")) {
				checkAfterReturning(method);
			} else if (method.getName().startsWith("ajc$afterThrowing$")) {
				checkAfterThrowing(method);
			} else if (method.getName().startsWith("ajc$around$")) {
				if (!method.getName().endsWith("proceed")) {
					checkAround(method);
				} else {
					adviceCount--;
				}
			} else {
				adviceCount--;
			}
		}
		if (adviceCount != 7) throw new RuntimeException("Expected 7 advice methods, found " + adviceCount);
	}
	
	private static void checkBefore(Method method) {
		assertTrue("expecting 2 annotations on before",method.getAnnotations().length == 2);
		Before beforeAnnotation = method.getAnnotation(Before.class);
		assertTrue("expecting  execution(* *.*(..))",beforeAnnotation.value().equals("execution(* *.*(..))"));
	}

	private static void checkAfter(Method method) {
		assertTrue("expecting 2 annotations on after",method.getAnnotations().length == 2);
		After afterAnnotation = method.getAnnotation(After.class);
		assertTrue("expecting  call(* Integer.*(..))",afterAnnotation.value().equals("call(* Integer.*(..))"));
	}
	
	private static void checkAfterReturning(Method method) {
		assertTrue("expecting 2 annotations on after returning",method.getAnnotations().length == 2);
		AfterReturning afterAnnotation = method.getAnnotation(AfterReturning.class);
		if (method.getParameterTypes().length == 1) {
			// form with returning arg
			assertTrue("expecting get(String *)",afterAnnotation.pointcut().equals("get(String *)"));
			assertTrue("expecting empty",afterAnnotation.value().equals(""));
			assertTrue("expecting strVal",afterAnnotation.returning().equals("strVal"));
		} else {
			// form without returning arg
			assertTrue("expecting get(String *)",afterAnnotation.pointcut().equals("get(String *)"));
			assertTrue("expecting empty",afterAnnotation.value().equals(""));
			assertTrue("expecting empty returning",afterAnnotation.returning().equals(""));
		}
	}

	private static void checkAfterThrowing(Method method) {
		assertTrue("expecting 2 annotations on after throwing",method.getAnnotations().length == 2);
		AfterThrowing afterAnnotation = method.getAnnotation(AfterThrowing.class);
		if (method.getParameterTypes().length == 1) {
			// form with returning arg
			assertTrue("expecting execution(* *.*(..))",afterAnnotation.pointcut().equals("execution(* *.*(..))"));
			assertTrue("expecting empty",afterAnnotation.value().equals(""));
			assertTrue("expecting ex",afterAnnotation.throwing().equals("ex"));
		} else {
			// form without returning arg
			assertTrue("expecting execution(* *.*(..))",afterAnnotation.pointcut().equals("execution(* *.*(..))"));
			assertTrue("expecting empty",afterAnnotation.value().equals(""));
			assertTrue("expecting empty throwing",afterAnnotation.throwing().equals(""));
		}
	}

	private static void checkAround(Method method) {
		assertTrue("expecting 2 annotations on around",method.getAnnotations().length == 2);
		Around aroundAnnotation = method.getAnnotation(Around.class);
		assertTrue("expecting  set(* foo)",aroundAnnotation.value().equals("set(* foo)"));
	}

	private static void assertTrue(String msg, boolean expr) {
		if (!expr) throw new RuntimeException(msg);
	}
}