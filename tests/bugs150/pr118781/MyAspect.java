package blah;

public aspect MyAspect {

       pointcut callPointCut(): call(public * blah.MyClass+.*(..));

       Object around() : callPointCut() {
               System.out.println("start of around");
               Object result = proceed();
               System.out.println("end of around");
               return result;
       }
}
