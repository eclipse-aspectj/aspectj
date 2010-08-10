public class F2 {
  public static void main(String []argv) throws Exception {
    System.out.println(F2.class.getAnnotations()[0]);
  }
}

aspect X extends Base2<String> {
}
