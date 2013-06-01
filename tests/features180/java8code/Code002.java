
interface I002 {
  public void one();
  default public void two() {
    System.out.println("two running");
  }
}

public class Code002 implements I002 {
  @Override
  public void one() {
    System.out.println("one running");
  }

  public static void main(String []argv) {
    Code002 c = new Code002();
    c.one();
    c.two();
  }
}
