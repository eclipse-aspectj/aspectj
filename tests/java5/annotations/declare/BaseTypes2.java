public class BaseTypes {

  public static void main(String []argv) {
    new A().m();
    new B().m();
    new C().m();
  }

}

class A {
  public void m() { System.err.println("A.m() running");}
}

class B extends A{
}

class C extends B{
}
