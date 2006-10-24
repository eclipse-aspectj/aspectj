import java.util.ArrayList;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

public class Main {

  public List<? extends Element> getElements() {
    return new ArrayList<Element>();
  }

  class Element {};

  @Aspect
  static abstract class Base<T> {
    @Around("call(List<? extends T> *.*(..))")
    public List<? extends T> elementList(ProceedingJoinPoint thisJoinPoint) {
      try {
        return (List<? extends T>)thisJoinPoint.proceed();
      } catch (Throwable e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Aspect
  static class Concrete extends Base<Element> {}

  public static void main(String[] args) {
    new Main().getElements();
  }

}