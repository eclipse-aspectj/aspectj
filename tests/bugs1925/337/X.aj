public aspect X {
  pointcut p(long l): call(* F.m(..)) && args(l);
  Object around (long id): p(id) { return null; }

  public static void main(String []argv) {
    new F().m(3L);
  }
}

class F {
    public void m(long r) {}
}
