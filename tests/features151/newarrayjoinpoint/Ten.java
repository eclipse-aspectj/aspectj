public class Ten {

  public static void main(String []argv) {
    Ten a = new Ten();
    int [] is = new int[5];
  }
}

aspect X {

  pointcut p(Object o): call(new(..)) && target(o) && within(Ten);

  before(Object o): p(o) {
    System.err.println("before "+o);
  }

  after(Object o): p(o) {
    System.err.println("after "+o);
  }

  after() returning(Object o): call(*.new(..)) && within(Ten) {
    System.err.println("afterReturning "+o.getClass());
  }

}