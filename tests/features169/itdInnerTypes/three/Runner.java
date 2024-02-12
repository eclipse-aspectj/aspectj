package a.b.c;

@RelatedType(value=Vote.__.class)
public class Runner {
  public static void main(String[]argv) {
    Vote.__ v = new Vote.__("wibble");
    System.out.println(v.getString());
    System.out.println(Runner.class.getDeclaredAnnotations()[0]);
  }
}
