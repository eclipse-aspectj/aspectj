
public class ThreadNoField {
    public static void main (String[] args) {
        Target.main(args);
    } 
}

aspect Aspect {
    static final String[] SIGNALS = new String[] 
    { "around - start"
    , "around - run - before proceed"
    , "around - run - after proceed"
    };
    /** @testcase PR#620 around advice inner Thread subclass running proceed but not writing field */
    int around(): Target.pointcutTarget() {
        Thread runner = new Thread() {
                public void run() {
                    Common.signal(SIGNALS[1]);
                    proceed();
                    Common.signal(SIGNALS[2]);
                }
            };
        runner.start();
        Common.joinWith(runner);
        Common.signal(SIGNALS[0]);
        return 1; // hard-wired since no result
    }    
}
