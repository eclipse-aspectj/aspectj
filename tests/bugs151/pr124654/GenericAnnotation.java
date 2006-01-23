import java.lang.annotation.Annotation;

public abstract aspect GenericAnnotation<A extends Annotation> {

   pointcut annotatedCall(A a) : call(@A * *.*(..)) && @annotation(a);

   before(A a) : annotatedCall(a) {
       System.err.println("Reference pointcut advice. "+a.annotationType());
   }

   before(A a) : call(@A * *.*(..)) && @annotation(a) {
       System.err.println("Inlined pointcut advice. "+a.annotationType());
   }

}
