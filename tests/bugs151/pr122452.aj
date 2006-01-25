public aspect pr122452 {
    pointcut greeting() : call (* Point.sayHello(..));
    pointcut greeting2() : call (* related.Hello.sayHello(..));
    after() returning() : greeting*() {
        System.out.println(" World!");
    }
}
