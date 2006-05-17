import org.aspectj.lang.annotation.*;

@Aspect
public class A {

        @Before("within(C) && call(* foo(..))")
        public void touchBeforeExecute() {
        	  System.err.println("foo called");
        }
}