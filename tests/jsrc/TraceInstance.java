aspect TraceAll extends Trace {
    pointcut targets(): receptions(!native * *(..)) && (instanceof(java..* || javax..*));
}
