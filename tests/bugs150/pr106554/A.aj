public aspect A {
  interface I { 
    // These fields will have their initialization logic stuffed in a static
    // initializer in A$I
    public static final String[] str = new String[] { "a","b"};
    public static final String[] str2 = str;
  }

  static Class[] classes = { I.class };

  static Object f = new Integer(1);

  public static void main(String args[]) {
    System.out.println("test = "+f);
    System.err.println("A:"+A.aspectOf());
  }
}

aspect StopsInit pertypewithin(A) {
  // These should match nothing in A$I
  before() : staticinitialization(*) {}
  before() : set(* *) && within(A$I) {}
  before() : get(* *) && within(A$I) {}
}
