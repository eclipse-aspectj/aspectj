

// Protected method in A
class A {
  protected void m1 (){System.err.println("A.m1()");}
}


// Simple subclass
public class PR83303 extends A {
  public static void main(String []argv) {
    System.err.println("Hi");
    new PR83303().m1();
  }
}


aspect C {
  declare parents: PR83303 implements I;
  public void PR83303.m1(){System.err.println("ITD version of m1");}
}


interface I {
  public void m1();
}
