package p;

public class B {

  public B() {
  }

  public static void main(String[] argv) {
    B b = new B();
	b.sayhi();
    b.sayhi();
    b.sayhi();
    
    if (!a.X.hasAspect(A.class)) {
    	throw new RuntimeException("hasAspect(A.class) should return true");
    }
    
    if (!a.X.hasAspect(B.class)) {
    	throw new RuntimeException("hasAspect(B.class) should return true");
    }
    
    if (a.X.hasAspect(q.D.class)) {
    	throw new RuntimeException("hasAspect(D.class) should return false");
    }
    
  }

  public void sayhi() { System.err.println("hi from B"); }

}
