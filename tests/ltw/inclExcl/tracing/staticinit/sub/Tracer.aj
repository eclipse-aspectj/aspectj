package tracing.staticinit.sub;

public aspect Tracer {
    before() : staticinitialization(pkg..*) {
        System.err.println("sub: "+thisJoinPoint);
    }
}
