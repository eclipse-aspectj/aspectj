aspect TraceAll extends Trace {
    pointcut targets(): call(!native * *(..)) && Pcds.myTarget();
}
