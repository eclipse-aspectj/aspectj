package symbols;

strictfp aspect A issingleton() {

    /** objects */
    pointcut instanceof_C(): this(C);
    pointcut hasaspect_A(): if(A.hasAspect());

    /** lexical extents */
    pointcut within_C(): within(C);
    pointcut withinall_C(): within(C+);
    pointcut withincode_C(): withincode(void C.*(..));

    /** control flow */
    pointcut cflow_C(): cflow(withincode_C());
    pointcut cflowtop_C(): cflow(withincode_C() && !cflowbelow(withincode_C()));

    /** methods and constructors */
    pointcut calls_C(): call(int C.*(..));
    pointcut receptions_C(): call(int C.*(..));
    pointcut executions_C(): execution(* C.*(..,float,..));
    //pointcut callsto_C(): callsto(call(void C.*()));

    /** exception handlers */
    pointcut handlers_Thr(): handler(java.lang.Throwable);
    pointcut handlers_Err(): handler(java.lang.Error);
    pointcut handlers_Exc(): handler(java.lang.Exception);
    pointcut handlers_Rt(): handler(java.lang.RuntimeException);

    /** fields */
    pointcut gets_f(): get(float C.*);
    pointcut sets_f(): set(float C.*);

    /** Advices */
    //before(): call(void C.*()) { }
    before(): this(C) && call(String Object.toString()) { }
    before(): execution(C.new()) { }
    after(): call(void C.*()) { }
    after() returning (int x): call(int C.*(..)) { }
    after() throwing (RuntimeException e): call(void C.MethV()) {
        throw new RuntimeException("test");
    }
    void around() : call(void C.MethV()) { proceed(); }

    /** Introductions */
    public double symbols.C.intrD;
    private void C.intrMethV() { intrD += 1; }

}
