import org.aspectj.testing.Tester;

public aspect AspectOnInterface /*of eachobject(instanceof(I1))*/ {
    before (I1 i1): target(i1) && call(String process()) {
	i1.addToS("-advised");
    }
    
    public static void main(String[] args) { test(); }

    public static void test() {
        ConcreteC1 c1 = new ConcreteC1();
        Tester.checkEqual(c1.process(), "foo-advised-processed", "");
    }
}
	
interface I1 {
    public void addToS(String newS);
    public String process();
}

class ConcreteC1 implements I1 {
    String s = "foo";
    public void addToS(String newS) { s += newS; }
    public String process() {
        return s + "-processed";
    }
}
