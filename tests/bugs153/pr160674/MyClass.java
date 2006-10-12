package a;

public class MyClass {
  public static void main(String []argv) {
    new MyClass().print("hello");
    new MyClass().print("world");
  }

  public void print(String msg) {
    System.out.println(msg);
  }
}
