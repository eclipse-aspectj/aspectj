
package app;
// WARNING: do not change lineation without changing test specification!!
public class Main {

    static String staticString = "hello";
    
    String s = "me";
    
    public static void main(String[] args) {

        String temp = staticString;        // 12

        staticString = temp + " world!";   // 14

        Main main = new Main();            // 16

        for (int i = 0; i < args.length; i++) {
            main.go(args[i]);              // 19
        }
    }

    Main() {                // 23
    }

    void go(String s) {     // 26
        try {

            String temp = this.s;  // 29

            this.s = temp + ", " + s;  // 31

        } catch (RuntimeException e) {   // 33
            
            stop();                      // 35 

        }
    }

    void stop() {                        // 40

        new Main();                      // 42

    }
}


class C {

    C() {                               // 50

    }

}

