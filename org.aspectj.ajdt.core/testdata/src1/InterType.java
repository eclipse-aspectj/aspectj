import junit.framework.Assert;

public class InterType {

	public static void main(String[] args) {
		Target t = new Target();
		Assert.assertEquals(0, t.i);
		t.i = 37;
		Assert.assertEquals(37, t.i);
		Assert.assertEquals(37, t.i++);
		Assert.assertEquals(38, t.i);
		Assert.assertEquals(39, ++t.i);
		Assert.assertEquals(39, t.i);
		
		System.out.println("t.i = " + t.i);
		
		Assert.assertEquals(0, ((I)t).i);
		
		Assert.assertEquals("foo", t.s);
		t.s += "-bar";
		Assert.assertEquals("foo-bar", t.s);
		System.out.println("t.s = " + t.s);
		
		// this captures an interesting property of initializers:
		// that introduced inits run before non-introduced inits
		Assert.assertEquals("inst00onA", A.getInstS(t));
		System.out.println("t.instS = " + A.getInstS(t));
		
		I i = t;
		Assert.assertEquals("dummy", i.myString);
		i.myString = "foo";
		Assert.assertEquals(i.myString, "foo");
		System.out.println("t.myString = " + t.myString);
		
		Assert.assertNotNull(A.aspectOf());
		Assert.assertEquals(A.aspectOf(), A.aspectOf());
		Assert.assertTrue(A.hasAspect());
		
		//XXX don't handle constants
//		int x = 10;
//		switch(x) {
//			case Target.CONST: break;
//			default: Assert.fail("bad switch");
//		}
		
	}
}

class Target implements I {
	public int j = 2;
	private boolean instS = true;  // potential conflict need to handle gracefully
	public boolean getInstS() { return instS; }
	
	String conflict = "boo";
}

class SubTarget extends Target {
	public static String foo() {
		return A.aspectOf().toString();
	}
}


interface I { }



aspect A {
	private static String aStatic = "onA";
	
	public int Target.foobarvas = 0;
	public int I.i = 0;
	public int Target.i;
	public static String Target.s = "foo";
	public static int Target.CONST = 10;
	
	private String Target.instS = "inst" + j + this.j + aStatic;
	
	//private String Target.conflict = "goo";  error
	
	public static String getInstS(Target t) {
		return t.instS;
	}
	
	public String I.myString = "dummy";
}
