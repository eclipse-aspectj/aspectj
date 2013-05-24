public class TypeAnnoOnLocalVariable {
  public void m() {
    System.out.println("Hello");
    @Anno String s = "abc";
    System.out.println(s);
  }
}
