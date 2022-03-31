import my.module.Modular;

public class NonModular {
  Modular modular = new Modular();

  public static void main(String[] args) {
    System.out.println("Non-modular class can use modular one");
  }
}
