package patterntesting.check.runtime;

public aspect NotNullAspect {

    pointcut ctorWithNotNullArg() :
        execution(*..*.new(*)) && @args(NotNull);

    before() : ctorWithNotNullArg() {
        Object[] args = thisJoinPoint.getArgs();
        if (args[0] == null) {
            throw new AssertionError("@NotNull constraint violated");
        }
    }

}
