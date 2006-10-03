package tracing.staticinit;

public aspect Tracer {
    before() : staticinitialization(pkg..*) {
        System.err.println(thisJoinPoint);
    }
}
