
/**
 * Ajc produces duplicated methods, and javac complains:
 * <pre>..\ajworkingdir\AlreadyDefined.java:51: 
 *   signal$method_call10(java.lang.String) is already defined in AlreadyDefined
 *   private void signal$method_call10(final String msg) </pre>
 */
public class IfPCDDupMethod {
    public static void main(String[] args) { }
}

aspect AlreadyDefined {

    before(): ifFalse(Object)   { signal(""); }
	// must come second
    pointcut ifFalse (Object t) : this(t) && if(t instanceof Runnable ) ; 

	// same result for execution or call or withincode
    after() : withincode(static void IfPCDDupMethod.main(String[])) {
		signal("");
	}

    static void signal(String msg) {
    }
}
