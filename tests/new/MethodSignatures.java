import org.aspectj.testing.Tester;
import java.util.Set;
import java.util.*;

interface I {
    public void m2();
}

class C {
    public void m0() {}
    public void m1() {}
    public void m2() {}
}

class D0 extends C {
    public void m1() { super.m1(); }
}

class D1 extends C implements I {
    public void m0() { super.m0(); }
    public void m2() { super.m2(); }
}

class E {

    Set set = new HashSet();

    String makePrettyString(String val) {
	return decode(val) + " advice for " + method(val);
    }

    public void check(String msg, String vals) {
        StringTokenizer tok = new StringTokenizer(vals);
        while (tok.hasMoreTokens()) {
            String val = tok.nextToken();
            Tester.check(set.contains(val),
                         msg + " does not contain " +
                         makePrettyString(val));
	    if (set.contains(val)) set.remove(val);
        }

	// check that nothing that shouldn't be set is set
	for (Iterator i = set.iterator(); i.hasNext(); ) {
	    Tester.check(false,
                         msg + " shouldn't contain " +
                         makePrettyString((String)i.next()));
	}

        clear();
    }

    public String method(String val) {
        int idollar = val.indexOf("$");
        if (idollar == -1) {
            return val;
        }
        return val.substring(idollar+1);
    }

    public String decode(String val) {
        int idollar = val.indexOf("$");
        if (idollar == -1) {
            return val;
        }
        String code = val.substring(0, idollar);
        String result = val;
        if (code.equals("bc")) {
            result = "before calls";
        } else if (code.equals("br")) {
            result = "before receptions";
        } else if (code.equals("be")) {
            result = "before execution";
        }
        return result;
    }

    public void put(String val) {
        set.add(val);
    }

    public void clear() {
        set = new HashSet();
    }
}

interface F {
    E e = new E();
}

public class MethodSignatures implements F {
    static int i = 1;
    static int j = 0;
    static int k = 0;
    public static void main(String[] args) {
	I id1 = new D1();
	C c   = new C();
	C cd0 = new D0();
	C cd1 = new D1();
	D0 d0 = new D0();
	D1 d1 = new D1();

//  	id1.m2(); e.check("id1.m2", "bc$i.m2 br$i.m2 be$i.m2  be$c.m2 be$d1.m2  br$c.m2 br$d1.m2");

//  	c.m0();   e.check("c.m0",   "bc$c.m0  br$c.m0  be$c.m0");
//  	c.m1();   e.check("c.m1",   "bc$c.m1  br$c.m1  be$c.m1");
//  	c.m2();   e.check("c.m2",   "bc$c.m2  br$c.m2  be$c.m2");
//  	cd0.m0(); e.check("cd0.m0", "bc$c.m0  br$d0.m0           be$c.m0 br$c.m0");
//  	cd0.m1(); e.check("cd0.m1", "bc$c.m1  br$d0.m1 be$d0.m1  be$c.m1 br$c.m1");
//  	cd0.m2(); e.check("cd0.m2", "bc$c.m2  br$d0.m2           be$c.m2 br$c.m2");
//  	cd1.m0(); e.check("cd1.m0", "bc$c.m0  br$d1.m0 be$d1.m0  br$c.m0 be$c.m0");
//  	cd1.m1(); e.check("cd1.m1", "bc$c.m1  br$d1.m1           br$c.m1 be$c.m1");
//  	cd1.m2(); e.check("cd1.m2", "bc$c.m2  br$d1.m2 be$d1.m2  br$c.m2 be$c.m2  br$i.m2 be$i.m2");
//  	d0.m0();  e.check("d0.m0",  "bc$d0.m0 br$d0.m0           bc$c.m0 br$c.m0 be$c.m0");  //??? bc$d0.m0
//  	d0.m1();  e.check("d0.m1",  "bc$d0.m1 br$d0.m1 be$d0.m1  bc$c.m1 br$c.m1 be$c.m1");
//  	d0.m2();  e.check("d0.m2",  "bc$d0.m2  br$d0.m2          bc$c.m2 br$c.m2 be$c.m2");  //??? bc$d0.m2
//  	d1.m0();  e.check("d1.m0",  "bc$d1.m0  br$d1.m0 be$d1.m0  bc$c.m0 br$c.m0 be$c.m0");
//  	d1.m1();  e.check("d1.m1",  "bc$d1.m1  br$d1.m1           bc$c.m1 br$c.m1 be$c.m1");  //??? bc$d1.m1
//  	d1.m2();  e.check("d1.m2",  "bc$d1.m2  br$d1.m2 be$d1.m2  bc$c.m2 br$c.m2 be$c.m2  bc$i.m2 br$i.m2 be$i.m2");
        id1.m2(); e.check("id1.m2", "bc$i.m2 be$i.m2  be$c.m2 be$d1.m2  ");

	c.m0();   e.check("c.m0",   "bc$c.m0   be$c.m0");
	c.m1();   e.check("c.m1",   "bc$c.m1   be$c.m1");
	c.m2();   e.check("c.m2",   "bc$c.m2   be$c.m2");
	cd0.m0(); e.check("cd0.m0", "bc$c.m0             be$c.m0");
	cd0.m1(); e.check("cd0.m1", "bc$c.m1   be$d0.m1  be$c.m1");
	cd0.m2(); e.check("cd0.m2", "bc$c.m2             be$c.m2 ");
	cd1.m0(); e.check("cd1.m0", "bc$c.m0   be$d1.m0  be$c.m0");
	cd1.m1(); e.check("cd1.m1", "bc$c.m1             be$c.m1");
	cd1.m2(); e.check("cd1.m2", "bc$c.m2   be$d1.m2  be$c.m2  be$i.m2");
	d0.m0();  e.check("d0.m0",  "bc$d0.m0            bc$c.m0 be$c.m0");  //??? bc$d0.m0
	d0.m1();  e.check("d0.m1",  "bc$d0.m1  be$d0.m1  bc$c.m1 be$c.m1");
	d0.m2();  e.check("d0.m2",  "bc$d0.m2            bc$c.m2 be$c.m2");  //??? bc$d0.m2
	d1.m0();  e.check("d1.m0",  "bc$d1.m0   be$d1.m0  bc$c.m0 be$c.m0");
	d1.m1();  e.check("d1.m1",  "bc$d1.m1             bc$c.m1 be$c.m1");  //??? bc$d1.m1
	d1.m2();  e.check("d1.m2",  "bc$d1.m2   be$d1.m2  bc$c.m2 be$c.m2  bc$i.m2 be$i.m2");
    }
}


aspect A implements F {

    before(): call(void C.m0())  { e.put("bc$c.m0"); }
    before(): call(void C.m1())  { e.put("bc$c.m1"); }
    before(): call(void C.m2())  { e.put("bc$c.m2"); }

    before(): call(void D0.m0()) { e.put("bc$d0.m0"); }
    before(): call(void D0.m1()) { e.put("bc$d0.m1"); }
    before(): call(void D0.m2()) { e.put("bc$d0.m2"); }

    before(): call(void D1.m0()) { e.put("bc$d1.m0"); }
    before(): call(void D1.m1()) { e.put("bc$d1.m1"); }
    before(): call(void D1.m2()) { e.put("bc$d1.m2"); }

    before(): call(void I.m2())  { e.put("bc$i.m2"); }


//      /*static*/ before(): call(void C.m0())  { e.put("br$c.m0"); }
//      /*static*/ before(): call(void C.m1())  { e.put("br$c.m1"); }
//      /*static*/ before(): call(void C.m2())  { e.put("br$c.m2"); }

//      /*static*/ before(): call(void D0.m0()) { e.put("br$d0.m0"); }
//      /*static*/ before(): call(void D0.m1()) { e.put("br$d0.m1"); }
//      /*static*/ before(): call(void D0.m2()) { e.put("br$d0.m2"); }

//      /*static*/ before(): call(void D1.m0()) { e.put("br$d1.m0"); }
//      /*static*/ before(): call(void D1.m1()) { e.put("br$d1.m1"); }
//      /*static*/ before(): call(void D1.m2()) { e.put("br$d1.m2"); }

//      /*static*/ before(): call(void I.m2())  { e.put("br$i.m2"); }


    /*static*/ before(): execution(void C.m0())  { e.put("be$c.m0"); }
    /*static*/ before(): execution(void C.m1())  { e.put("be$c.m1"); }
    /*static*/ before(): execution(void C.m2())  { e.put("be$c.m2"); }

    /*static*/ before(): execution(void D0.m0()) { e.put("be$d0.m0"); } // no targets
    /*static*/ before(): execution(void D0.m1()) { e.put("be$d0.m1"); }
    /*static*/ before(): execution(void D0.m2()) { e.put("be$d0.m2"); } // no targets

    /*static*/ before(): execution(void D1.m0()) { e.put("be$d1.m0"); }
    /*static*/ before(): execution(void D1.m1()) { e.put("be$d1.m1"); } // no targets
    /*static*/ before(): execution(void D1.m2()) { e.put("be$d1.m2"); }

    /*static*/ before(): execution(void I.m2())  { e.put("be$i.m2"); }
}
