public privileged aspect TestAspect2 {
       pointcut TestInheritance(Test2 test) : target(test) && execution (* Generic1.*(..));

       after (Test2 test) : TestInheritance(test) {
               System.err.println("Aspects:"+thisJoinPoint);
       }
}

