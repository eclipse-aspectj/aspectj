abstract aspect SimpleTracing perthis(tracedCall())
{
    abstract pointcut tracedCall();

    before(): tracedCall() {
        System.out.println("Entering: " + thisJoinPoint);
    }
}
