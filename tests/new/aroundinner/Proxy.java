
public class Proxy {
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
    class Proxy { int result; }

    /** @testcase PR#620 around advice inner Runnable running proceed and writing method-final proxy */
    int around(): Target.pointcutTarget() {
        final Proxy proxy = new Proxy();
        Runnable runner = new Runnable() {
                public void run() {
                    Common.signal(SIGNALS[1]);
                    proxy.result = proceed();
                    Common.signal(SIGNALS[2]);
                }
            };
        runner.run();
        Common.signal(SIGNALS[0]);
        return proxy.result;
    }    
}
