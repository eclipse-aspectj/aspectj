package a.b.c;

@RelatedType(value=Vote._.class)
public class Runner {
  public static void main(String[]argv) {
    Vote._ v = new Vote._("wibble");
    System.out.println(v.getString());
    System.out.println(Runner.class.getDeclaredAnnotations()[0]);
  }
}

