import org.aspectj.testing.Tester;

public class AroundCalls {
    public static void main(String[] args) { test(); }

    public static void test() {
	//Tester.checkEqual(new C().m(), "abc:2", "many arounds");
        Tester.checkEqual(new C().m(), "acb:2", "many arounds");
    }
}

class C {
    public String m() {
	return new D().m1("a", 0);
    }
}

class D {
    public String m1(String s, int x) { return s + ":" + x;  }
}

aspect A {
    String around(D d, String as, int ax):
        call(String D.m1(String,int)) &&
        args(as,ax) &&
        target(d)
        //receptions(String d.m1(as, ax))

        {
            //System.out.println(as + " : " + d + " : " + ax);
	return proceed(d, as + "c", ax + 1);
    }
    
    String around(String as/*, C c1*/, D d1, int ax):
        within(C) &&
        target(d1) && call(String m1(String,int)) && args(as,ax)
        //instanceof(c1) && callsto(instanceof(d1) && receptions(String m1(as, ax)))
    {
        //System.out.println(as + " : " + c1 + " : " + d1 + " : " + ax);
	return proceed(as + "b", /*c1,*/ d1, ax + 1);
    }
}
