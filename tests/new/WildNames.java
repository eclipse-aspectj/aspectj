import org.aspectj.testing.Tester;

public aspect WildNames {
    public static void main(String[] args) { test(); }

    public static void test() {
	C c = new C();

	c.getFoo(); c.getBar();
	Tester.check("getFoo"); Tester.check("getBar");

	c.fooGetter(); c.barGetter();
	Tester.check("fooGetter"); Tester.check("barGetter");

	c.prefixFooSuffix(); c.prefixBarSuffix();
	Tester.check("prefixFooSuffix"); Tester.check("prefixBarSuffix");
    }

    /*static*/ after() returning (String s): execution(String get*()) {
	Tester.checkEqual("get*", s);
	Tester.note(thisJoinPoint.getSignature().getName());
    }
    /*static*/ after() returning (String s): execution(String *Getter()) {
	Tester.checkEqual("*Getter", s);
	Tester.note(thisJoinPoint.getSignature().getName());
    }
    /*static*/ after() returning (String s): execution(String prefix*Suffix()) {
	Tester.checkEqual("prefix*Suffix", s);
	Tester.note(thisJoinPoint.getSignature().getName());
    }
}

class C {
    public String getFoo() { return "get*"; }
    public String getBar() { return "get*"; }

    public String fooGetter() { return "*Getter"; }
    public String barGetter() { return "*Getter"; }

    public String prefixFooSuffix() { return "prefix*Suffix"; }
    public String prefixBarSuffix() { return "prefix*Suffix"; }
}
