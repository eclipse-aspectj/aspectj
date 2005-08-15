// Decp an interface with an ITD method on it
abstract aspect GenericAspect<A> {

  interface SimpleI {}

  declare parents: A implements SimpleI;

  public int SimpleI.m() { return 4;}

}

aspect GenericAspectC extends GenericAspect<Base> {
  public static void main(String []argv) {
    Base b = new Base();

    if (!(b instanceof SimpleI)) 
      throw new RuntimeException("Base should implement SimpleI!");

    int i = b.m();
  }
}

class Base {}

