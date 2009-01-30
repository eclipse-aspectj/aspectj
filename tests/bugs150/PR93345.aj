// "XLint warning for advice not applied with cflow(execution)"

class AClass {
}

aspect AnAspect {
        pointcut a() : execution(* *(..)) && cflow( execution(* *(..)) );

        before() : a() {
                System.out.println("before a");
        }
}