import org.aspectj.testing.Tester;

public class MultiAndAround {
    public static void main(String[] args) {
        exercise(new Base());
        exercise(new Derived());
        exercise(new SubBase());

        Tester.checkEventsFromFile("MultiAndAround.out");
    }

    static void exercise(IBase b) {
        Tester.event("**************************************");
        b.base1("1");
        b.base2("2");
        b.base3("3");
    }
}


interface IBase {
    void base1(String s);
    void base2(String s);
    void base3(String s);
}

interface IDerived extends IBase {
    void base1(String s);
}

class Base implements IBase {
    public void base2(String s) { Tester.event("Base.base2"); }
}

class Derived implements IDerived {
    public void base3(String s) { Tester.event("Derived.base3"); }
}

class SubBase extends Base {
    public void base3(String s) { Tester.event("SubBase.base3"); }
}

aspect Intro {
    public void IBase.base1(String s) { Tester.event("IBase.base1 from Intro"); }
    public void IBase.base3(String s) { Tester.event("IBase.base3 from Intro"); }

    public void IDerived.base1(String s) { Tester.event("IDerived.base1 from Intro"); }
    public void IDerived.base2(String s) { Tester.event("IDerived.base2 from Intro"); }
    public void IDerived.base3(String s) { Tester.event("IDerived.base3 from Intro"); }
}


aspect A {
    Object around(): call(* IBase.*(..)) {
        Tester.event("around call(IBase.*)");
        return proceed();
    }
    Object around(): call(* IBase+.*(..)) {
        Tester.event("around call(IBase+.*)");
        return proceed();
    }
    Object around(): call(* Base.*(..)) {
        Tester.event("around call(Base.*)");
        return proceed();
    }
    Object around(): execution(* Base+.*(..)) {
        Tester.event("around execution(Base+.*)");
        return proceed();
    }
}
