package test.aspects;

public aspect TestAspect {

       pointcut boundaries(): execution (public * *..*MBean+.*(..));

       Object around(): boundaries() {
               return proceed();
       }
}
