import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.JoinPoint;

/**
 * https://github.com/eclipse-aspectj/aspectj/issues/162
 */
public interface InterfaceWithInnerClass {
  public class ImplicitlyStatic {
    public int getNumber() {
      return 11;
    }

    public static void main(String[] args) {
      System.out.println(new ImplicitlyStatic().getNumber());
    }
  }

  /*static*/ aspect MyAspect {
    before() : execution(* main(..)) {
      System.out.println(thisJoinPoint);
    }
  }

  @Aspect
  /*static*/ class MyAnnotationAspect {
    @Before("execution(* getNumber(..))")
    public void myAdvice(JoinPoint thisJoinPoint){
      System.out.println(thisJoinPoint);
    }
  }
}
