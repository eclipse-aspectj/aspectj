import org.aspectj.testing.Tester;

class C {
    public static String staticField = "initialized";
    
    public String state = "C-initialized";
    
    public C() {
        state = "C-constructed";
    }
}

class SubC extends C implements I {
    {
        state = "SubC-initialized";
    }
    
    public SubC() {
        state = "SubC-constructed";
    }
}

interface I {
    public static String s = "initialized";
    public static String s1 = new String("s1");
}


aspect A issingleton () {
    before(): staticinitialization(C) {
        Tester.checkEqual(C.staticField, null, "C.staticField");
    }
    after(): staticinitialization(C) {
        Tester.checkEqual(C.staticField, "initialized", "C.staticField");
        Tester.note("static initialized C");
    }
    after(): staticinitialization(SubC) {
        Tester.note("static initialized SubC");
    }
    /*
      before(): staticinitializations(I) {
      Tester.checkEqual(I.s, null, "I.s");
      }
      after(): staticinitializations(I) {
      Tester.checkEqual(I.s, "initialized", "I.s");
      Tester.note("static initialized I");
      }
    */
    
    
    before(C c): initialization(C.new(..)) && this(c) {
        Tester.checkEqual(c.state, null, "c.state");
    }

    before(C c): execution(C.new(..)) && this(c) {
    	// change from 1.0 is that fields aren't initialized at this point
        Tester.checkEqual(c.state, null, "c.state pre-constructor"); //"C-initialized", "c.state");
        Tester.note("constructed C");
    }

    after(C c): initialization(C.new(..)) && this(c) {
        Tester.checkEqual(c.state, "C-constructed", "c.state");
        Tester.note("initialized C");
    }
    

    before(SubC subc): initialization(SubC.new(..)) && this(subc) {
        Tester.checkEqual(subc.state, "C-constructed", "c.state");
    }
    before(SubC subc): execution(SubC.new(..)) && this(subc) {
    	// change from 1.0 is that fields aren't initialized at this point
        Tester.checkEqual(subc.state, "C-constructed", "c.state"); //"SubC-initialized", "c.state");
        Tester.note("constructed SubC");
    }

    after(SubC subc): initialization(SubC.new(..)) && this(subc) {
        Tester.checkEqual(subc.state, "SubC-constructed", thisJoinPoint.toString());
        Tester.note("initialized SubC");
    }
    
    before(I i): initialization(I.new()) && this(i) {
        Tester.checkEqual(((C)i).state, "C-constructed", thisJoinPoint.toString());
    }
//    before(I i): execution(I.new()) && this(i) {
//        Tester.checkEqual(((C)i).state, "C-constructed", thisJoinPoint.toString());
//        Tester.note("constructed I");
//    }
    after(I i): initialization(I.new()) && this(i) {
        Tester.checkEqual(((C)i).state, "C-constructed", thisJoinPoint.toString());
        Tester.note("initialized I");
    }
}

public class InitializerTest {
    public static void main(String[] args) {
        new SubC();
        Tester.check("initialized C");
        Tester.check("initialized SubC");
        Tester.check("constructed C");
        Tester.check("constructed SubC");

        Tester.check("initialized I");
        //Tester.check("constructed I");

        Tester.check("static initialized C");
        Tester.check("static initialized SubC");
        //XXX not doing static initializers in interfaces yet
        //XXX Tester.check("static initialized I");
    }
}
