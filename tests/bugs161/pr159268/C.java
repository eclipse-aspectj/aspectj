public class C {

  public static void main(String []argv) {
    Object[] before;
    before = new Object[10];
    before[0].toString(); // Eclipse: Syntax error on token "before", invalid
  }
}
