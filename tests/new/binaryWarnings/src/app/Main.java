
package app;
// WARNING: do not change lineation without changing test specification!!
public class Main {

    static String staticString = "hello";
    
    String s = "me";     // bug 8 initializer picked out as execution
    
    public static void main(String[] args) {

        String temp = staticString;        // 12

        staticString = temp + " world!";   // 14

        Main main = new Main();            // 16

        for (int i = 0; i < args.length; i++) {
            main.go(args[i]);              // 19
        }
    }

    Main() {  s += "pick me, not initializer";   // 23
    }

    void go(String s) {  String t = "..".substring(0);   // 26
        try {

            String temp = this.s;  // 29

            this.s = temp + ", " + s;  // 31
            D.go();                      // 32 
        } catch (RuntimeException e) {   String u = "..".substring(0);  // 33
            
            stop();                      // 35 

        }
    }

    void stop() {                        // 40

        D.go();                      // 42

    }
}


class C {

    C() {                               // 50

    }

}

class D {
    
    static void go() {
        
    }

}
