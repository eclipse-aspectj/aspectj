import org.aspectj.lang.annotation.*;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

public class Code3 {

  public Object createBean(String name) {
    return 37;
  }

  public static void main(String[] args) {
    Code3 factory = new Code3();
    System.out.println("Code3.main returned from call to createBean "+factory.createBean("s"));
  }
}

@Aspect
class AfterReturningTestAspect {

  @AfterReturning(pointcut = "call(Object createBean(String)) && args(beanName)", returning = "bean")
  // this does not run
  public void afterReturningCreateBean(/*JoinPoint joinPoint,*/ String beanName, Object bean) throws Throwable {
  // this matches:
  //public void afterReturningCreateBean(JoinPoint joinPoint, Object bean, String beanName) throws Throwable {
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

