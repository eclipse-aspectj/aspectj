public class pr110906 {

  public static void main(String []argv) {
    new pr110906().printNames(new Object[]{"a","b","c"});
    new pr110906().printNames("a","b","c"); // should be allowed!!
    new pr110906().printNames2("a","b","c"); // should be allowed!!
  }

}

aspect A {

  private interface HasName {}

  declare parents: (pr110906) implements HasName;

  public void HasName.printNames(Object... names) {
    System.out.println(names[0]);
  }

  public void HasName.printNames2(String... names) {
    System.out.println(names[0]);
  }

}
