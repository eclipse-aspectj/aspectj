import org.aspectj.lang.JoinPoint.StaticPart;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

//@Retention(RetentionPolicy.RUNTIME)
@interface NormalException {
    /** The default of Void means ANY throwable */
    Class[] value() default Void.class; 
}

public aspect ErrorHandling {

    before(Throwable throwable) : handler(*) && args(throwable) && !@withincode(NormalException) {
    	System.err.println("Caught in "+thisEnclosingJoinPointStaticPart.getSignature().getName());
    }    

    public static void main(String argz[]) {
    	new Test().checkConnection();
    }
}

class Test {
    @NormalException(Exception.class)
    protected void checkConnection() {
        try {
            foo();
        } catch (Exception e) {
        	;//skip warning
        }
    }
    
    private void foo() {
    	try {
    		throw new RuntimeException();
    	} catch (RuntimeException e) {
    		throw e;
    	}
    }

}
