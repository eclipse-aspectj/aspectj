//disallow defining more than one pointcut with the same name (PR#385)
aspect OverloadedPointcuts {
    static void foo(int i) {}
    pointcut fooCut(): execution(void OverloadedPointcuts.foo(int));
    pointcut fooCut(int i): execution(void OverloadedPointcuts.foo(int)) && args(i);
}
