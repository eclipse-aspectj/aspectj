aspect TraceSets extends Trace {
    pointcut targets(): Pcds.withinMe() && set(* *..*.*);
}
