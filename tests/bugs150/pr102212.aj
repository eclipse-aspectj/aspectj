interface Interface {}
abstract class Parent {}
class Child extends Parent implements Interface {}

public aspect pr102212 {
  // illegal modifier combination not caught by ajc
  public abstract synchronized void Parent._abstract();

  public synchronized void Child._abstract() {}


  // the following is legal - it's a default implementation....
  public /* implicit abstract */ synchronized void Interface._interface() {}


  // use Child to make java complain: "illegal modifiers: 0x421"
  // (this corresponds to "public abstract synchronized")
  public static void main(String[] args) {
    new Child();
  }
}

