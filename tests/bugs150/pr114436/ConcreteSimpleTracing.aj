aspect ConcreteSimpleTracing extends SimpleTracing
{
    pointcut tracedCall(): execution(void doSomething(String));
}
