import org.aspectj.testing.Tester;

public class ExternalCalls {
    public static void main(String[] args){
        Tester.checkEqual(new Test().go(), 1003);

        Tester.checkEqual(Math.max(1, 3), 3);
    }
}

class Test {
    int go(){
        return Math.max(1, 3);
    }
}

aspect A percflow(this(Test) && execution(* go(..))) { 
    // ! call(* Test.go()) shouldn't do anything
    int around(): call(int Math.*(..)) && ! call(* Test.go()) {
        return proceed() + 1000;
    }
}
