package tracing;

public aspect Tracer {
    before() : execution(* foo()) {
        System.err.println(thisJoinPoint);
    }
}
