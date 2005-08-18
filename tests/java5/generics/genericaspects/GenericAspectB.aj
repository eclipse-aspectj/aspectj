// Decp a generic interface
abstract aspect GenericAspect<A> {

  declare parents: A implements SimpleI;

  interface SimpleI<X> {}

}

aspect GenericAspectB extends GenericAspect<Base> {
  public static void main(String []argv) {
    Base b = new Base();
    if (!(b instanceof SimpleI)) 
      throw new RuntimeException("Base should implement SimpleI!");
  }
}

class Base {}

