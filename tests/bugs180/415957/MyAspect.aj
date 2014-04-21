public aspect MyAspect {
    pointcut all(): execution(@javax.annotation.Resource * *(..));


    before(): all() {
        System.out.println("Hi");
    }
}
