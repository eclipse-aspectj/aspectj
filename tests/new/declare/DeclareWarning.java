
/** @testcase PR#31724 omnibus declare-warning test */
public class DeclareWarning {                    
    static {                                     
        if (null == System.getProperty("ignore")) { // CW 5
            throw new Error("never thrown");
        }
    }
    static int staticInt;
    int instanceInt;
    public static void main (String[] args) {
        DeclareWarning dw = new DeclareWarning();  // CW 12..18   
        int i = staticInt;                       
        i = dw.instanceInt; 
        staticInt = 2;
        dw.instanceInt = 2;
        run();
        dw.irun();
        throw new Error("compile-only test");
    }
    public static void run() {}                    // CW 21..23
    public void irun() {}
    public DeclareWarning() {
        try {
            long l = System.currentTimeMillis();
            if (0l == l) {
                throw new Error("never thrown");
            } else if (1l == l) {
                throw new RuntimeException("never thrown");
            } else if (2l == l) {
                throw new OutOfMemoryError("never thrown");
            }
        } catch (OutOfMemoryError e) {                 
            // CW 34       
            System.err.println("never run"); 
        } catch (Error e) {                 
            // CW 37        
            System.err.println("never run"); 
        } catch (RuntimeException x) {                 
            // CW 40        
            System.err.println("never run"); 
        }
    }
}

aspect A {
    declare warning: staticinitialization(DeclareWarning)   
        : "staticinitialization(DeclareWarning)";
    declare warning: initialization(DeclareWarning.new(..))   
        : "initialization(DeclareWarning)";
    declare warning: get(int staticInt)   : "get staticInt";
    declare warning: get(int instanceInt) : "get instanceInt";
    declare warning: set(int staticInt)   : "set staticInt";
    declare warning: set(int instanceInt) : "set instanceInt";
    declare warning: call(void run())     : "call(void run())";
    declare warning: call(void irun())    : "call(void irun())";
    declare warning: call(DeclareWarning.new())     
        : "call(DeclareWarning.new())";
    declare warning: execution(void run())  : "execution(void run())";
    declare warning: execution(void irun()) : "execution(void irun())";
    declare warning: execution(DeclareWarning.new())     
        : "execution(DeclareWarning.new())";
    declare warning: handler(Error)        : "handler(Error)";
    declare warning: handler(OutOfMemoryError) && within(DeclareWarning)
                : "handler(OutOfMemoryError) && within(DeclareWarning)";
    declare warning: handler(RuntimeException) 
        && withincode(DeclareWarning.new())
                : "handler(RuntimeException) && withincode(DeclareWarning.new())";
    declare warning: adviceexecution() && within(A)
            : "adviceExecution() && within(A)";

    before() : initialization(DeclareWarning.new(..)) { // CW 72
       
        long l = System.currentTimeMillis();
        if (0l == l) {
            throw new Error("never thrown");
        }
    }
}
