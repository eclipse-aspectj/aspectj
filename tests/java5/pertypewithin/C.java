package p;
import a.X;

public class C {

  public C() {
  }

  public static void main(String[] argv) {
    
    C c = new C();
	c.sayhi();
    c.sayhi();
    c.sayhi();
    c.sayhi();
    
    if (a.X.aspectOf(A.class)==null) {
    	throw new RuntimeException("aspectOf(A.class) should not be null");
    }
    
    if (a.X.aspectOf(B.class)==null) {
    	throw new RuntimeException("aspecfOf(B.class) should not be null");
    }
    
    try {
    	Object o = a.X.aspectOf(q.D.class);
    	throw new RuntimeException("aspectOf(D.class) should be null");
    } catch (org.aspectj.lang.NoAspectBoundException nabe) {
    }
    
    a.X instanceForA = a.X.aspectOf(A.class);
    
    a.X instanceForB = a.X.aspectOf(B.class);
    
    if (instanceForA.equals(instanceForB)) {
    	throw new RuntimeException("Instances for A("+instanceForA+") and for B("+instanceForB+") should not be the same!");
    }
    
    // Now lets check the counts don't interfere
    
    A aa = new A();
    B b = new B();
    c = new C();
    X.aspectOf(A.class).setI(0);
    X.aspectOf(B.class).setI(0);
    X.aspectOf(C.class).setI(0);
    
    aa.sayhi();
    b.sayhi();
    aa.sayhi();
    c.sayhi();
    b.sayhi();
    aa.sayhi();
    aa.sayhi();
    
    if (a.X.aspectOf(A.class).getI()!=4) {
    	throw new RuntimeException("For A, i should be 4 but it is "+a.X.aspectOf(A.class).getI());
    }
    if (a.X.aspectOf(B.class).getI()!=2) {
    	throw new RuntimeException("For B, i should be 2 but it is "+a.X.aspectOf(B.class).getI());
    }
    if (a.X.aspectOf(C.class).getI()!=1) {
    	throw new RuntimeException("For C, i should be 1 but it is "+a.X.aspectOf(C.class).getI());
    }
    
  }

  public void sayhi() { System.err.println("hi C"); }

}
