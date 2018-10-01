public aspect MyAspect {
    pointcut all(): execution(@Resource * *(..));


    before(): all() {
        System.out.println("Hi");
    }
}
