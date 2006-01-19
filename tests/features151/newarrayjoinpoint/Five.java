// different advice kinds on the newarray jp
public class Five {
  public static void main(String []argv) {
    Integer[] Is = new Integer[5];
  }
}

aspect X {
  before(): call(new(..)) && within(Five) { System.err.println("before");}
}
aspect Y {
  after(): call(new(..)) && within(Five){System.err.println("after");}
  after() returning: call(new(..)) && within(Five){System.err.println("after returning");}
}
aspect Z {
  Integer[] around(): call(new(..)) && within(Five){System.err.println("around!"); return new Integer[500]; }
}
