
import org.aspectj.testing.Tester;

public class AddObjectOnAbstract {
    
    advice (I1 i1): i1 && String process() {
	    before {	    
            i1.addA();
	    }   
    }
    
    public static void main(String[] args) { test(); }

    public static void test() {
        AddObjectOnAbstract a = new AddObjectOnAbstract();
        ConcreteC1 c1 = new ConcreteC1();
        a.addObject(c1);
        Tester.checkEqual(c1.process(), "ab", "");
    }
}
	
abstract class I1 {
    public String s = "";
    public abstract void addA();
    public abstract String process();  
}

class ConcreteC1 extends I1 {
    public void addA() {
        s += "a";
    }
    public String process() {
        s += "b";
        return s;
    }
}