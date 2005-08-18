import java.util.*;
abstract aspect GenericAspect<A> {

  interface SimpleI<N extends Number> {}

  declare parents: A implements SimpleI<Integer>;

  public List<N> SimpleI<N>.ln;
  public J SimpleI<J>.n;

}

aspect GenericAspectF extends GenericAspect<Base> {
  public static void main(String []argv) {
    Base b = new Base();

    b.ln=new ArrayList<Integer>();
    b.n=new Integer(5);
  }
}

class Base {}

