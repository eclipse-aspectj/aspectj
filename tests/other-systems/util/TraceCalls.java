aspect TraceCalls extends Trace {
    pointcut targets(): Pcds.withinMe() && (call(* *(..)) || call(new(..)));
}
