
public class ThreadSub {
    public static void main (String[] args) {
        Target.main(args);
    } 
}

// need subclass - introduction fails on Thread as outside CCC
class ThreadSubclass extends Thread {
    int result;
}

aspect Aspect {
    static final String[] SIGNALS = new String[] 
    { "around - start"
    , "around - run - before proceed"
    , "around - run - after proceed"
    };
    /** @testcase PR#620 around advice inner Thread subclass running proceed and writing field */
    int around(): Target.pointcutTarget() {
        ThreadSubclass runner = new ThreadSubclass() {
                public void run() {
                    Common.signal(SIGNALS[1]);
                    result = proceed(); // remove to avoid bug
                    Common.signal(SIGNALS[2]);
                }
            };
        runner.start();
        Common.joinWith(runner);
        Common.signal(SIGNALS[0]);
        return runner.result; // remove to avoid bug
    }    
}
