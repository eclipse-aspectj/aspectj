import org.aspectj.testing.Tester;

public class InnerSuper {
    public static void main(String[] args) {
	Counter c = new C().makeCounter();
	c.count();
	Tester.checkEqual(c.n, 1, "counted");
    }
}

class C {
    public Counter makeCounter() {
	return new Counter() {
		public void count() {
		    n+=1;
		}
	    };
    }

    public InnerCounter makeInnerCounter() {
	class MyCounter extends InnerCounter {
	    public void count() {
		n += 1;
		toString();
	    }
	    public void lookat(Object o) {
		boolean b = o.equals("abc");
	    }
	}

	return new MyCounter();
    }


    protected class InnerCounter {
	protected int n;
	protected Object o;
    }

}

class Counter {
    protected int n = 0;
    public void count() {}
}
