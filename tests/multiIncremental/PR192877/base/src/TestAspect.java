import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareParents;

@Aspect
public class TestAspect {
   @DeclareParents(value="Foo+",defaultImpl=DefaultTestImpl.class)
   public Test implementedInterface;

   @Before("execution(* Foo.doSomething()) && this(t)")
   public void verifyRunningSender(Test t) {
       t.methodA();
       t.methodB();
   }
   
   public static void main(String[] args) {
       Foo foo = new FooImpl();
       foo.doSomething();
   }
}