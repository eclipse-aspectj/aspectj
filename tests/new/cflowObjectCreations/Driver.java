
import org.aspectj.testing.Tester;

import java.util.*;

// PR#307 cflow and object creations


public class Driver {

    public static String s = "";
    
    public static void main(String[] args){
        new FTPServer();
        
        Tester.checkEqual(s, "-connected-after", "");
    }  
}

/* PR306 */
class FTPServer {
    public FTPServer() {
        new FTPConnection().connect();
    }
}

class FTPConnection { 
    public void connect() {
        Driver.s += "-connected";    
    }
}

aspect FooBuilding percflow(serverIdentification(FTPServer)) {
    pointcut serverIdentification(FTPServer s) :
        target(s) && execution(new(..));

    after() returning (Object ret):
        target(FTPConnection) && call(* *(..))  {
            Driver.s += "-after";
    }
}
