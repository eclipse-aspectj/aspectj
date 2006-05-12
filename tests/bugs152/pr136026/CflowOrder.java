import java.io.PrintStream;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @interface Marker {}

class A {
	@Marker void foo() { }
    public static void main(String[] args) {
        new A().foo();
    }
}

public class CflowOrder {

    public static void main(String[] args) {
        A.main(null);
    }

    
    
    
    static aspect MyAspect  {

        pointcut annotated(Marker a) :  execution(@Marker * *(..)) && @annotation(a);

        pointcut belowAnnotated() :         cflowbelow(annotated(Marker));
        
//        pointcut belowAnnotated() :         cflowbelow(execution(@Marker * *(..)) && @annotation(Marker));
        
      pointcut topAnnotated(Marker a) : annotated(a) && !belowAnnotated();

      pointcut notTopAnnotated(/*Marker a,*/ Marker aTop) : 
    	                                       /* annotated(a) &&*/ cflowbelow(annotated(aTop));
      
        // if this first, then no nonTopAnnotated advice
        before(Marker a) : topAnnotated(a) { }
        
        // if topAnnotated is first, this does not run
        before(Marker aTop) : notTopAnnotated( aTop) { }
    }
}

