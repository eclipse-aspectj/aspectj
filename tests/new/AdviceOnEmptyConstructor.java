import org.aspectj.testing.Tester;

public aspect AdviceOnEmptyConstructor {
    public static void main(String[] args) { test(); }

    public static void test() {
        // C has an implied empty constructor so should be advised
        Tester.checkEqual(new C().value, "afterInit:foo", "new C");

        // C1 has no implied empty constructor (make sure we aren't cheating)
        Tester.checkEqual(new C1(0).value, "foo", "new C1");
    }
    
    /*static*/ after() returning (C c):
        //this(c) &&
        call(C.new()) {
        c.value = "afterInit:" + c.value;
    }
    /*static*/ after() returning(C1 c1):
        //this(c1) &&
        call(C1.new()) {
        c1.value = "afterInit:" + c1.value;
    }
}

class C {
    public String value = "foo";
}

class C1 {
    public String value = "foo";
    public C1(int dummy) {}
}
