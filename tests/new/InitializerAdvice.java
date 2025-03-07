import org.aspectj.testing.Tester;

public aspect InitializerAdvice {
    public static void test() {
	Tester.checkEqual(Foo.getStaticField(), "initialized", "static");
	Tester.checkEqual(new Foo().getInstanceField(), "initialized", "instance");
    }

    public static void main(String[] args) { test(); }

    before(): set(* *) && this(Foo) {
	    System.out.println("entering");
    }
}

class Foo {
    static String staticField = "";
    static {
	staticField = "initialized";
    }

    String instanceField = "";
    {
	instanceField = "initialized";
    }

    public static String getStaticField() {
	return staticField;
    }

    public String getInstanceField() {
	return instanceField;
    }
}
	
