public class pr106874 {
  class Inner { // works if static
    private Integer field;
    void bug() {
      field = new Integer(0);
    }
  }

  public static void main(String[] args) {
    new pr106874().new Inner().bug();
  }
}

aspect Aspect {
  before(Object t) :
    // target(Object) && // works
    // this(t) && // works
    target(t) && // fails
    // set(* Bug.Inner.field) // works
    // set(Integer Bug.Inner.*) // works
    // get(* Bug.Inner.*) // works
    set(* pr106874.Inner.*) // fails
  {}
}