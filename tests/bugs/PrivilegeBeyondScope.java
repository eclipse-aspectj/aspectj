
import java.util.Observable;


/** @testcase PR#906 privileged access out of code the compiler controls */
public class PrivilegeBeyondScope {

    public static void main (String[] args) {
        new C().get(); 
        throw new Error("expected compiler error");
    } 
}

class C {
    Object get() {return null;}
}

privileged aspect A {
    Observable observable = new Observable();

    after() returning (Object o ) : 
        execution(Object C.get()) {
        observable.setChanged(); // CE 23 (unable to implement privilege outside C
          // CE unable to implement privilege outside code the compiler controls
    }
}
