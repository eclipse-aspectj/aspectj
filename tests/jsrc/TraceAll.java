aspect TraceAll extends Trace of eachJVM() {
    pointcut targets(): receptions(!native * *(..)) && (instanceof(java..*|| javax..* || org.aspectj..*));
}
