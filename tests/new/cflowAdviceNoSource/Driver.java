
import org.aspectj.testing.Tester;

// PR#285 cflow advice with no source

public class Driver {

    public static String s = "";

    public static void main(String[] args){ test(); }

    public static void test() {
        new Driver().go();
        Tester.checkEqual(s, "-go-advised", "advice runs");
    }

    int go(){
        s += "-go";
        return Math.max(1, 3);
    }
}

aspect A of eachcflow(instanceof(Driver) && executions(* go(..))) {

   before (): calls(* Math.*(..)) && ! calls(* Driver.go()) && ! calls(* Driver.test()) {
        Driver.s += "-advised";
   }
}

