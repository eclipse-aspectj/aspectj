@Aspect
public class TestEmptyPointcutAtAspect {

       @Pointcut("")
       protected void scope () {}
}
