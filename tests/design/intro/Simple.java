import org.aspectj.testing.Tester;

public class Simple {
    public static void main(String[] args) {
	C c = new C();
	I i = (I)new C("hi");
	Tester.checkEqual(c.foo(), "foo:bar");
	Tester.checkEqual(i.mumble(), "mumble:foo:bar");

	Tester.checkEqual(A.Cm(), "from A");
	Tester.checkEqual(B.Cm(), "from B");

	c.idata = "c-mumble";
	Tester.checkEqual(c.idata, "c-mumble");
	Tester.checkEqual(i.idata, "mumble");

	Tester.check("new A.C");
	Tester.check("new B.C");
    }
}

class C {
    public C() { super(); }
    public String bar() {return "bar"; }
}

interface I { 
    String foo();
    String bar();
}


aspect A {
    private String C.data = "foo";
    private String C.data1 = this.data;

    public String I.idata = "mumble";

    public String C.foo() {
        String s = this.data;
        Tester.checkEqual(s, data1);
	return data + ":" + bar();
    }

    declare parents: C implements I;

    public String I.mumble() {
	return idata + ":" + foo();
    }

    private String C.m() {
	return "from A";
    }

    public static String Cm() {
	return new C(2).m();
    }
    
    public C.new(String s) { this(); }

    private C.new(int i) {
        this(); Tester.note("new A.C");
    }
}

aspect B {
    private String C.data = "B";

    private String C.m() {
	return "from " + data;
    }

    public static String Cm() {
	return new C(2).m();
    }

    private C.new(int i) {
	this(); Tester.note("new B.C");
    }
}    
