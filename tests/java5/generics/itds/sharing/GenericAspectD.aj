// Decp an interface with an ITD field
abstract aspect GenericAspect<A> {

  interface SimpleI {}

  declare parents: A implements SimpleI;

  public int SimpleI.n;

}

aspect GenericAspectD extends GenericAspect<Base> {
  public static void main(String []argv) {
    Base b = new Base();

    if (!(b instanceof SimpleI)) 
      throw new RuntimeException("Base should implement SimpleI!");

    b.n=42;
  }
}

class Base {}

