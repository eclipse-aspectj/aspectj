import org.aspectj.testing.*;

public class DeclareWarningMain {
    public static void main (String[] args) {  // DW 4 main
        Tester.checkAllEvents();
    } 
    static {
        Tester.expectEvent("before");
    }
}

aspect Warnings {
    declare warning : execution(static void main(String[])) : "main"; // for DW 4 main

    // just to show that pointcut is valid - works if warning removed
    before() : execution(static void main(String[])) {
        Tester.event("before");
    }
}
