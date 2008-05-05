package hello;

public class HW {
  public static void main(String[] argv) {
    new HW().print("Hello");
    new HW().print(" ");
    new HW().print("World");
    new HW().print("\n");
  }

  public void print(String msg) {
    System.out.print(msg);
  }
}
