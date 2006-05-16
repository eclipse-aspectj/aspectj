public aspect PerThisWithReference perthis(mypc()) {

  private static int id = 0;
  
  public PerThisWithReference() {
    id++;
  }
  
  public String toString() {
    return "PerThisWithReference:" + id;
  }

  public static void main(String[] args) {
    new C().foo();
    new C().foo();
  }

  pointcut mypc() : SomeOtherType.pc() && within(C);

  before() : mypc() {
    System.out.println("before " + this);
  }

}

class C {

 public void foo() {}

}