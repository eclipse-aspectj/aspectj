public class WhatsGoingOn {
    static public void main (String [] args) { }
}

aspect TraceClass {
    static private int level = 0;

    static private void offset() {
        for (int i = 0; i < level; ++i)  System.err.print("  ");
    }

    pointcut pc() : 
        !cflow(within(Trace*))
//        !(within(Trace*) || cflowbelow(within(Trace*)))
        ;
    before () : pc() {
        offset();
        System.err.println("-> " + thisJoinPoint);
        ++level;
    }
    after  () : pc() {
        --level;
        offset();
        System.err.println("<- " + thisJoinPoint);
    }
} 

aspect TraceTrace {

    static private int level = 0;

    static private void offset() {
        for (int i = 0; i < level; ++i)         System.err.print("  ");
    }
    
    pointcut pc() : within(TraceClass);
    before () : pc() {
        offset();
        System.err.println("=> " + thisJoinPoint);
        ++level;
    }
    after  () : pc() {
        --level;
        offset();
        System.err.println("<= " + thisJoinPoint);
    }
}

//Expected output:
//"=> staticinitialization(TraceClass.<clinit>)\n"+
//"  => set(int TraceClass.level)\n"+
//"  <= set(int TraceClass.level)\n"+
//"  => preinitialization(TraceClass())\n"+
//"  <= preinitialization(TraceClass())\n"+
//"  => initialization(TraceClass())\n"+
//"    => execution(TraceClass())\n"+
//"    <= execution(TraceClass())\n"+
//"  <= initialization(TraceClass())\n"+
//"<= staticinitialization(TraceClass.<clinit>)\n"+
//"=> execution(ADVICE: void TraceClass.ajc$before$TraceClass$1$346234(JoinPoint))\n"+
//"  => call(void TraceClass.offset())\n"+
//"    => execution(void TraceClass.offset())\n"+
//"      => get(int TraceClass.level)\n"+
//"      <= get(int TraceClass.level)\n"+
//"    <= execution(void TraceClass.offset())\n"+
//"  <= call(void TraceClass.offset())\n"+
//"  => get(PrintStream java.lang.System.err)\n"+
//"  <= get(PrintStream java.lang.System.err)\n"+
//"  => call(java.lang.StringBuffer(String))\n"+
//"  <= call(java.lang.StringBuffer(String))\n"+
//"  => call(StringBuffer java.lang.StringBuffer.append(Object))\n"+
//"  <= call(StringBuffer java.lang.StringBuffer.append(Object))\n"+
//"  => call(String java.lang.StringBuffer.toString())\n"+
//"  <= call(String java.lang.StringBuffer.toString())\n"+
//"  => call(void java.io.PrintStream.println(String))\n"+
//"-> staticinitialization(WhatsGoingOn.<clinit>)\n"+
//"  <= call(void java.io.PrintStream.println(String))\n"+
//"  => get(int TraceClass.level)\n"+
//"  <= get(int TraceClass.level)\n"+
//"  => set(int TraceClass.level)\n"+
//"  <= set(int TraceClass.level)\n"+
//"<= execution(ADVICE: void TraceClass.ajc$before$TraceClass$1$346234(JoinPoint))\n"+
//"=> execution(ADVICE: void TraceClass.ajc$after$TraceClass$2$346234(JoinPoint))\n"+
//"  => get(int TraceClass.level)\n"+
//"  <= get(int TraceClass.level)\n"+
//"  => set(int TraceClass.level)\n"+
//"  <= set(int TraceClass.level)\n"+
//"  => call(void TraceClass.offset())\n"+
//"    => execution(void TraceClass.offset())\n"+
//"      => get(int TraceClass.level)\n"+
//"      <= get(int TraceClass.level)\n"+
//"    <= execution(void TraceClass.offset())\n"+
//"  <= call(void TraceClass.offset())\n"+
//"  => get(PrintStream java.lang.System.err)\n"+
//"  <= get(PrintStream java.lang.System.err)\n"+
//"  => call(java.lang.StringBuffer(String))\n"+
//"  <= call(java.lang.StringBuffer(String))\n"+
//"  => call(StringBuffer java.lang.StringBuffer.append(Object))\n"+
//"  <= call(StringBuffer java.lang.StringBuffer.append(Object))\n"+
//"  => call(String java.lang.StringBuffer.toString())\n"+
//"  <= call(String java.lang.StringBuffer.toString())\n"+
//"  => call(void java.io.PrintStream.println(String))\n"+
//"<- staticinitialization(WhatsGoingOn.<clinit>)\n"+
//"  <= call(void java.io.PrintStream.println(String))\n"+
//"<= execution(ADVICE: void TraceClass.ajc$after$TraceClass$2$346234(JoinPoint))\n"+
//"=> execution(ADVICE: void TraceClass.ajc$before$TraceClass$1$346234(JoinPoint))\n"+
//"  => call(void TraceClass.offset())\n"+
//"    => execution(void TraceClass.offset())\n"+
//"      => get(int TraceClass.level)\n"+
//"      <= get(int TraceClass.level)\n"+
//"    <= execution(void TraceClass.offset())\n"+
//"  <= call(void TraceClass.offset())\n"+
//"  => get(PrintStream java.lang.System.err)\n"+
//"  <= get(PrintStream java.lang.System.err)\n"+
//"  => call(java.lang.StringBuffer(String))\n"+
//"  <= call(java.lang.StringBuffer(String))\n"+
//"  => call(StringBuffer java.lang.StringBuffer.append(Object))\n"+
//"  <= call(StringBuffer java.lang.StringBuffer.append(Object))\n"+
//"  => call(String java.lang.StringBuffer.toString())\n"+
//"  <= call(String java.lang.StringBuffer.toString())\n"+
//"  => call(void java.io.PrintStream.println(String))\n"+
//"-> execution(void WhatsGoingOn.main(String[]))\n"+
//"  <= call(void java.io.PrintStream.println(String))\n"+
//"  => get(int TraceClass.level)\n"+
//"  <= get(int TraceClass.level)\n"+
//"  => set(int TraceClass.level)\n"+
//"  <= set(int TraceClass.level)\n"+
//"<= execution(ADVICE: void TraceClass.ajc$before$TraceClass$1$346234(JoinPoint))\n"+
//"=> execution(ADVICE: void TraceClass.ajc$after$TraceClass$2$346234(JoinPoint))\n"+
//"  => get(int TraceClass.level)\n"+
//"  <= get(int TraceClass.level)\n"+
//"  => set(int TraceClass.level)\n"+
//"  <= set(int TraceClass.level)\n"+
//"  => call(void TraceClass.offset())\n"+
//"    => execution(void TraceClass.offset())\n"+
//"      => get(int TraceClass.level)\n"+
//"      <= get(int TraceClass.level)\n"+
//"    <= execution(void TraceClass.offset())\n"+
//"  <= call(void TraceClass.offset())\n"+
//"  => get(PrintStream java.lang.System.err)\n"+
//"  <= get(PrintStream java.lang.System.err)\n"+
//"  => call(java.lang.StringBuffer(String))\n"+
//"  <= call(java.lang.StringBuffer(String))\n"+
//"  => call(StringBuffer java.lang.StringBuffer.append(Object))\n"+
//"  <= call(StringBuffer java.lang.StringBuffer.append(Object))\n"+
//"  => call(String java.lang.StringBuffer.toString())\n"+
//"  <= call(String java.lang.StringBuffer.toString())\n"+
//"  => call(void java.io.PrintStream.println(String))\n"+
//"<- execution(void WhatsGoingOn.main(String[]))\n"+
//"  <= call(void java.io.PrintStream.println(String))\n"+
//"<= execution(ADVICE: void TraceClass.ajc$after$TraceClass$2$346234(JoinPoint))\n");