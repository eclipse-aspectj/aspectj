import junit.framework.Assert;

public class InterTypeConstructors {

	public static void main(String[] args) {
		Target t = A.makeT();
	}
}

class Target implements I {
	private Target(String s) {
		System.err.println(s);
	}
	
	public Target() {
		this(10);
	}
	
	int f = 10;
	static int ss() { return 100; }

	String getS() { return "Target" + this.instS("Foo"); }
	
	private int instS(String s) { return 10; }
	
	public int mmmm() { System.err.println("noarg"); return 3;}
	
}

class C {
	class SubTarget extends Target {
		final String s;
		
		public SubTarget() {
			super(10);
			s = "hi";
		}
	}
}

//class SubTarget extends Target {
//	String getS() { return "SubTarget"; }
//	static int ss() { return 10; }
//	
//	public void foobar() {
//		mmmm(333);
//	}
//	
//	//public int mmmm(int x) { return x; }
//}

interface SuperI {}


interface I extends SuperI { }



privileged aspect A {
	public static Target makeT() {
		return new Target(10);
	}
	
	Target.new(int foo) {
		this("hi" + ++foo);
		this.f = mmmm() + foo;
		System.err.println(f == 14);
	}
	
	//C.SubTarget.new(String value) {  // uncomment for an error
		//this.s = value;
	//}
}
