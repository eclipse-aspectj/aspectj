package bugs;

import java.io.PrintStream;
import java.lang.annotation.*;

import org.aspectj.lang.JoinPoint;

public class CflowOrderOriginal {

    public static void main(String[] args) {
        Log.print("Starting CflowOrder.main(..)");
        A.main(null);
        Log.print("Ending CflowOrder.main(..)");
    }

    @Retention(value = RetentionPolicy.RUNTIME)
    @interface Annotation {
        String value();
    }

    static class A {
        @Annotation("A.foo")
        void foo() {
            new B().foo();
            Log.print("A.foo()");
        }

        public static void main(String[] args) {
            new A().foo();
            Log.print("A.main(..)");
        }
    }

    static class B {
        @Annotation("B.foo")
        void foo() {
            Log.print("B.foo()");
        }
    }

    static class Log implements IAspect {
        static final PrintStream out = System.err;

        static void print(String label) {
            out.println(label);
        }

        static void print(String label, JoinPoint tjp, JoinPoint.StaticPart sp,
                Object a) {
            out.println(label);
//            out.println(" Join point: " + tjp);
//            out.println(" Enclosing join point: " + sp);
//            out.println(" Annotation: " + a);
        }
    }
    static aspect Logger implements IAspect {

        //declare error: execution(* *(..)) && !within(Log) : "er";

//        before() : cflow(execution(void CflowOrder.main(String[]))) 
//            && !call(* IAspect+.*(..)) && ! within(IAspect+) {
//            Log.print("cflow(..main(..))", thisJoinPoint,
//                    thisEnclosingJoinPointStaticPart, null);
//        }
    }

    interface IAspect {}
    static aspect MyAspect  implements IAspect {

        pointcut annotated(Annotation a) :
            call(@Annotation * *(..)) && @annotation(a);

        pointcut belowAnnotated() :
            cflowbelow(annotated(Annotation));
      pointcut topAnnotated(Annotation a) : annotated(a) 
            && !belowAnnotated();

      pointcut notTopAnnotated(Annotation a, Annotation aTop) : annotated(a) 
      && cflowbelow(annotated(aTop));
//        pointcut topAnnotated(Annotation a) : annotated(a) 
//            && !cflowbelow(annotated(Annotation));
//
//        pointcut notTopAnnotated(Annotation a, Annotation aTop) : annotated(a) 
//            &&  cflowbelow(topAnnotated(aTop));

        // if this first, then no nonTopAnnotated advice
        before(Annotation a) : topAnnotated(a) {
            Log.print("topAnnotated", thisJoinPoint,
                    thisEnclosingJoinPointStaticPart, a);
        }
        // if topAnnotated is first, this does not run
        before(Annotation a, Annotation aTop) : notTopAnnotated(a, aTop) {
            Log.print("nonTopAnnotated", thisJoinPoint,
                    thisEnclosingJoinPointStaticPart, a);
        }
    }
}

