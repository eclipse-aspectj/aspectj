
package language;

public class Context {
    public static void main(String[] argList) {
        new C().run();
    }
}

class C {
    static int MAX = 2;
    int i;
    C() {
        i = 1;
    }
    public void run() {
        try {
            more();
        } catch (MoreError e) {
            // log but continue
            System.out.println(e.getMessage());
        }
    }

    private void more() {
        i++;
        if (i >= MAX) {
            i = 0;
            throw new MoreError();
        }
    }
    static class MoreError extends Error {
        MoreError() {
            super("was too much!");
        }
    }
}

/** @author Erik Hilsdale, Wes Isberg */
aspect A {

    // START-SAMPLE language-fieldSetContext Check input and result for a field set.
    /** 
     * Check input and result for a field set.
     */
    void around(int input, C targ) : set(int C.i) 
            && args(input) && target(targ) {
        String m = "setting C.i="  + targ.i  + " to " + input;
        System.out.println(m);
        proceed(input, targ);
        if (targ.i != input) {
            throw new Error("expected " + input);
        }
    }
    // END-SAMPLE language-fieldSetContext

    // START-SAMPLE language-handlerContext Log exception being handled
    /** 
     * Log exception being handled
     */
    before (C.MoreError e) : handler(C.MoreError) 
            && args(e) && within(C) {
        System.out.println("handling " + e);
    }
    // END-SAMPLE language-handlerContext

    // See Initialization.java for constructor call,
    // constructor execution, and {pre}-initialization
    
}