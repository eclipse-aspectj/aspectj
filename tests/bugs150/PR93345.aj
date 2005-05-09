// "XLint warning for advice not applied with cflow(execution)"

class AClass {
}

aspect AnAspect {
        pointcut a() : cflow( execution(* *(..)) );

        before() : a() {
                System.out.println("before a");
        }
}