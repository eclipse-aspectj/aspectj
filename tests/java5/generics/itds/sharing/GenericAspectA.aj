// Simple - adding an interface to a type via a generic aspect and decp
abstract aspect GenericAspect<A> {

  declare parents: A implements SimpleI;

  interface SimpleI {}

}

aspect GenericAspectA extends GenericAspect<Base> {
  public static void main(String []argv) {
    Base b = new Base();
    if (!(b instanceof SimpleI)) 
      throw new RuntimeException("Base should implement SimpleI!");
  }
}

class Base {}

