public privileged aspect TestAspect {
       pointcut TestInheritance(Test test) : target(test) && execution (* Generic1.*(..));

       after (Test test) : TestInheritance(test) {
               System.err.println("Aspects:"+thisJoinPoint);
       }
}

