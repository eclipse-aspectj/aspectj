
public class RunnableSub {
    public static void main (String[] args) {
        Target.main(args);
    } 
}

interface MyRunner extends Runnable {}

aspect Aspect {
    static final String[] SIGNALS = new String[] 
    { "around - start"
    , "around - run - before proceed"
    , "around - run - after proceed"
    };

    // introduced field on interface
    int MyRunner.result; 

    /** @testcase PR#620 around advice inner Runnable (subinterface) running proceed and writing field introduced on subinterface */
    int around(): Target.pointcutTarget() {
        MyRunner runner = new MyRunner() {
                public void run() {
                    Common.signal(SIGNALS[1]);
                    result = proceed(); // remove to avoid bug
                    Common.signal(SIGNALS[2]);
                }
            };
        runner.run();
        Common.signal(SIGNALS[0]);
        return runner.result; // remove to avoid bug
    }    
}

