
import org.aspectj.testing.Tester;

// PR#290 compiler crashes with eachobject and named pointcuts with parameters

public class Driver {

    public static String s = "";
    
    public static void main(String[] args){
        new C().go();
        Tester.checkEqual(s, "-before-go", "");
    }
   
}

class C {
    int x;
    
    public void go() {
        Driver.s += "-go";
    }
}

aspect A /*of eachobject(A.testPointcut(C))*/ {
    pointcut testPointcut(C c): target(c);

    before(C c): target(c) && call(* *(..)) {
	    Driver.s += "-before";
    }
}
