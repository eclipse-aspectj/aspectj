package p;

// This class does *nothing* clever, but does match the advice in X

public class A {

  public A() {
  }

  public static void main(String[] argv) {
  	A anA = new A();
    anA.sayhi();
    anA.sayhi();
   
    System.err.println("Tests in A have passed");
  }

  public void sayhi() { System.err.println("hi from A"); }

}
