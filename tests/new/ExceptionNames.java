
import org.aspectj.testing.Tester;

// PR#125

public class ExceptionNames {

    public static void main(String[] args) { test(); }

    public static void test() {
        String exception = "";
	    java.lang.reflect.Method liveRoutine = null;
	    try {
		    liveRoutine.invoke(null,null);
	    }
	    catch (java.lang.reflect.InvocationTargetException e) {
	        System.out.println(" " + e.getTargetException());
	        exception = e.getClass().toString();
	    }
	    catch (Exception e) {
	        exception = e.getClass().toString();
	    }
	    Tester.checkEqual(exception, "class java.lang.NullPointerException", "exception handled");
    }
    private void foo() {}
}
