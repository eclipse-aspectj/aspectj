
/** @testcase PR721 interface declaration not permitted in local method scope */
public class LocalInterfaceCE {
    void method() {
        interface Local {}   // CE 5 interface not allowed here
    } 
    static {
        interface Local {}   // CE 8 interface not allowed here
    }
    static void staticMethod() {
        interface Local {}   // CE 11 interface not allowed here
    }
    class Inner {
        void method() {
            interface Local {}   // CE 15 interface not allowed here
        } 
    }
}
