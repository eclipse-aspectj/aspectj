abstract aspect GenericAspect<A> {

  declare parents: A implements IUtil;

  //public void IUtil<Z>.print(Z n) { System.err.println(n); }
}

interface IUtil<N extends Number> { }

aspect GenericAspectE extends GenericAspect<Base> {
  public static void main(String []argv) {
    Base b = new Base();
    // b.print("hello");
  }
}

class Base {}

