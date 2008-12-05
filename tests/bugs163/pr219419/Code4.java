import org.aspectj.lang.annotation.*;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

public class Code4 {

  public Object createBean(String name) {
    return 37;
  }

  public static void main(String[] args) {
    Code4 factory = new Code4();
    System.out.println("Code4.main returned from call to createBean "+factory.createBean("s"));
  }
}

@Aspect
class AfterReturningTestAspect {

  @AfterReturning(pointcut = "call(Object createBean(String)) && args(beanName)", returning = "bean")
  public void afterReturningCreateBean(Object bean, String beanName) throws Throwable {
    System.out.println("afterReturningCreateBean advice input='" + beanName + "' ret=" + bean);
  }

  /*
  @AfterReturning(pointcut = "call(Object aspects.SimpleAfterReturningTest.createBean(String)) " +
				                                "&& args(beanName)", returning = "bean")
			        public void afterReturningCreateBean(JoinPoint joinPoint, Object bean,
						String beanName) throws Throwable {
					        System.out.println("afterReturningCreateBean(JoinPoint joinPoint,
						Object bean, String beanName) for '" + beanName + "'=" + bean);
						    }
						    */
}

