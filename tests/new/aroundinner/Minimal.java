
public class Minimal {
    public static void main (String[] args) {
        Target.main(args);
    } 
}

class MyObject { int result = 0; }
aspect Aspect {
    static final String[] SIGNALS = new String[] 
    { "around - run - before proceed"
    , "around - run - after proceed"
    };

    /** @testcase PR#620 around advice inner class running proceed and writing field */
    int around(): Target.pointcutTarget() {
        MyObject o = new MyObject() { 
                void ignored() { 
                    result = 1; // remove to avoid bug
                } 
            };
        Common.signal(SIGNALS[0]);
        int i = proceed(); 
        Common.signal(SIGNALS[1]);
        return i; 
    }    
}

