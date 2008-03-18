package p;

public class A {
  public static void main(String []argv) {
    ((IFace)new A()).foo();
    System.out.println("ok");
  }
}
