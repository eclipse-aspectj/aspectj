aspect TraceMembers extends Trace {
    pointcut targets(): Pcds.withinMe() && execution(!abstract !native * *(..));
}
