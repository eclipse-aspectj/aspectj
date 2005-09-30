class Base {
public    int x;
}

aspect ExtendBase {
  public ExtendBase() {}
//    private int ExtendBase.x;
    declare parents: Y extends ExtendBase;
}

class Y extends Base {
    public void foo() {
	System.out.println(x);
    }
}
