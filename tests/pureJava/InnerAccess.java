import org.aspectj.testing.Tester;

import java.util.*;

public class InnerAccess {
    public static void main(String[] args) {
	Tester.checkEqual(new C().getCount(), 3);
    }
}


class C {
    protected int i = 2;
    private String s = "hi";

    Runnable r = new Runnable() {
	    public void run() {
		s += "s";       
	    }
	};

    public int getCount() {
	return new Object() {
		public int m() {
		    r.run();
		    return s.length();
		}
	    }.m();
    }
}

class DI extends D.Inner {
}


class D implements Map.Entry {
    public Object getKey() { return null; }
    public Object getValue() { return null; }
    public Object setValue(Object o) { return o; }

    static class Inner {}
}


class Outer {
    class Middle {
	class Inner {
	    void m() {
		Inner.this.m1();
		Middle.this.m1();
		Outer.this.m1();
	    }

	    void m1() {}
	}
	void m1() {}
    }
    void m1() {}
}

