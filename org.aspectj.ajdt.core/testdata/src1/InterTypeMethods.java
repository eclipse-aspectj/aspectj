import junit.framework.Assert;

public class InterTypeMethods {

	public static void main(String[] args) {
		Target t = new Target();
		Assert.assertEquals(23, t.instM(1));
		Assert.assertEquals("foo10", A.instS(t, "foo"));
		
		System.out.println("t: " + t);
		
		Assert.assertEquals(12, SubTarget.staticM());
		
		SubTarget st = new SubTarget();
		
		Assert.assertEquals("100Target10", st.foo());
		
		st.foobar();
		t.mmmm(2);
		
		Assert.assertEquals("xyz-foo", st.onI("xyz-"));
	}
}

class Target implements I {
	int f = 10;
	static int ss() { return 100; }

	String getS() { return "Target" + this.instS("Foo"); }
	
	private int instS(String s) { return 10; }
	
	public int mmmm() { System.err.println("noarg"); return 3;}
	
}

class SubTarget extends Target {
	String getS() { return "SubTarget"; }
	static int ss() { return 10; }
	
	public void foobar() {
		mmmm(333);
	}
	
	//public int mmmm(int x) { return x; }
}

interface SuperI {}


interface I extends SuperI { }



aspect A {
	public static String instS(Target t, String s) {
		return t.instS(s);
	}
	
	public int Target.instM(int x) { return x+2 + f + this.f; }
	private String Target.instS(String s) { return s + this.f; }
	
	static int SubTarget.staticM() { return ss() + 2; }
	
	public String SubTarget.foo() { return super.ss() + super.getS(); }
	
	public int Target.mmmm(int i) { System.err.println("onearg"); return 3;}
	public int SubTarget.mmmm(int i) { System.err.println("onearg on Sub"); return 4;}
	
	public String I.v = "foo";
	
	public String I.onI(String a) {
		return a + v;
	}
	
	public String SuperI.onI(String a) {
		return a + "Super";
	}
}
