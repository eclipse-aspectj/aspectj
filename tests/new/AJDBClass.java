public class AJDBClass {

    public      int publicInt = 0;
    protected   int protectedInt = 1;
    private     int privateInt = 2;
    /*package*/ int packageInt = 3;

    public       AJDBClass()         {}
    /*package*/  AJDBClass(int i)    {}
    protected    AJDBClass(byte b)   {}
    private      AJDBClass(String s) {}

    {
        publicInt = 13;
    }
    
    public static void main(String[] args) {
        System.out.println("Starting...");
        new AJDBClass().go();
        System.out.println("Done.");
    }

    void go() {
        int j = 1;
        String string = "string";
        byte b = (byte)9;
        long l = 123123123;
        double d = 123.123;
        short s = (short)4;
        char c = 'c';
        Object o = null;
        Integer integer = new Integer(13);
        a();
        b();
        c();
        d();
    }

    public void a() {
        System.out.println("a");
    }
    
    protected void b() {
        System.out.println("b");
    }

    private void c() {
        System.out.println("c");
    }

    void d() {
        System.out.println("d");
    }
}

aspect Aspect {
    pointcut ours():          instanceof(AJDBClass);
    pointcut allReceptions(): receptions(void *(..)) && ours();
    pointcut allExecutions(): executions(void *(..)) && within(AJDBClass);
    pointcut allCalls():      calls(void AJDBClass.*(..));
    pointcut allCallsTo():    callsto(receptions(void *(..)) && instanceof(AJDBClass));

    static before(): allReceptions() {
        System.out.println("before receptions: " + thisJoinPoint);
    }
    static after():  allReceptions() {
        System.out.println("after receptions: " + thisJoinPoint);
    }
    static around() returns void: allReceptions() {
        System.out.println("around before receptions: " + thisJoinPoint);
        proceed();
        System.out.println("around after receptions: " + thisJoinPoint);
    }

    static before(): allExecutions() {
        System.out.println("before executions: " + thisJoinPoint);
    }
    static after():  allExecutions() {
        System.out.println("after executions: " + thisJoinPoint);
    }
    static around() returns void: allExecutions() {
        System.out.println("around before executions: " + thisJoinPoint);
        proceed();
        System.out.println("around after executions: " + thisJoinPoint);
    }

    static before(): allCalls() {
        System.out.println("before calls: " + thisJoinPoint);
    }
    static after():  allCalls() {
        System.out.println("after calls: " + thisJoinPoint);
    }
    static around() returns void: allCalls() {
        System.out.println("around before calls: " + thisJoinPoint);
        proceed();
        System.out.println("around after calls: " + thisJoinPoint);
    }

    static before(): allCallsTo() {
        System.out.println("before callsTo: " + thisJoinPoint);
    }
    static after():  allCallsTo() {
        System.out.println("after callsTo: " + thisJoinPoint);
    }
    static around() returns void: allCallsTo() {
        System.out.println("around before callsTo: " + thisJoinPoint);
        proceed();
        System.out.println("around after callsTo: " + thisJoinPoint);
    }        
}
