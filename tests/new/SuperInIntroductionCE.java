
import org.aspectj.testing.Tester;

public class SuperInIntroductionCE {
    public static void main (String[] args) {
        int result = new Sub().getInt();
        Tester.check(2==result, "new Sub().getInt() !2==" + result);
    } 
}

class Super {
    private int privateInt = 1;
    private int privateIntMethod() { return privateInt; }
    int defaultedInt = 1;
    int defaultIntMethod() { return privateInt; }
}

class Sub extends Super { }

class ObjectSub { }

aspect A {
    /** @testcase accessing private and default method and field of class within code the compiler controls */
    public int Sub.getInt() {
        int result = super.privateInt;       // CE 25 expected here
        result += super.privateIntMethod();  // CE 26 expected here
        // todo: move A and Super to separate packages
        //result += defaultInt;                // CE expected here
        //result += defaultIntMethod();        // CE expected here
        return result;
    }
}
