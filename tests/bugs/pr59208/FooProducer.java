public class FooProducer {

  public static final int N_METHODS = 50;
  public static final int N_STATEMENTS = Short.MAX_VALUE/(2*N_METHODS);  
  public static void main(String[] args) {
    System.out.println("public class Foo {");
    System.out.println("static java.util.Set hs = new java.util.HashSet();");
    for (int i = 0; i < N_METHODS; i++) {
      System.out.println("public void test" + i + "() {");
      for (int j=0; j < N_STATEMENTS; j++) {
        System.out.println("hs.add(new Object());");
      } 
      System.out.println("}");
    }
    System.out.println("}");
  }

}
