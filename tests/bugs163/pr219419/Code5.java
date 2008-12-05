import org.aspectj.lang.annotation.*;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

public class Code5 {

  public Object createBean(String name) {
    return 37;
  }

  public static void main(String[] args) {
    Code5 factory = new Code5();
    System.out.println("Code5.main returned from call to createBean "+factory.createBean("s"));
  }
}

@Aspect
class AfterReturningTestAspect {

  @AfterReturning(pointcut = "call(Object createBean(String)) && args(beanName)", returning = "bean")
  public void afterReturningCreateBean(JoinPoint joinPoint, String beanName, Object bean) throws Throwable {
    System.out.println("afterReturningCreateBean advice input='" + beanName + "' ret=" + bean);
  }

}

