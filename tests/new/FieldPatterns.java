import org.aspectj.testing.Tester;

public class FieldPatterns {
    public static void main(String[] args) {
	SuperC sc = new SuperC();
	C c = new C();
	SubC subc = new SubC();

	Tester.checkEqual(sc.name, "SuperC");
	Tester.checkEqual(sc.count, 1, "SuperC");

	Tester.checkEqual(c.name, "C");
	Tester.checkEqual(c.count, 1, "C");

	Tester.checkEqual(subc.name, "C");
	Tester.checkEqual(subc.count, 1, "SubC");

	Tester.checkEqual(((SuperC)c).name, "SuperC");
	Tester.checkEqual(c.count, 2, "C as SuperC");

        c.name = null;
    }
}


class SuperC {
    int count = 0;
    String name = "SuperC";
}

class C extends SuperC {
    String name = "C";
}

class SubC extends C {
}

aspect A {
    before(SuperC sc): get(String SuperC.name) && target(sc){
	sc.count++;
    }
    before(C c): get(String C.name) && target(c) {
	c.count++;
    }

    before(): set(String C.name) {
    }
}

    
