public class F1 {
  public static void main(String []argv) throws Exception {
    System.out.println(F1.class.getAnnotations()[0]);
  }
}

aspect X extends Base {
}
