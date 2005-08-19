// Testing interactions between ITD methods and the fields that might exist
// on a generic type...
import java.util.*;
import java.lang.reflect.*;
import org.aspectj.lang.annotation.*;

class I<A> {
  A a;
  List<A> b;
}

aspect Foo {

  C I<C>.m0() {
    return a;
  }

  List<C> I<C>.m1() {
    return b;
  }

  void I<C>.setA(C aC) {
    a = aC;
  }

  void I<L>.setB(List<L> aB) {
    b = aB;
  }

  List<C> I<C>.swap(List<C> cs1,List<C> cs2) {
    cs1=cs2;
    return cs1;
  }

}

public class GenericAspectQ extends I<String> {

  public static void main(String []argv) { 
    GenericAspectQ _instance = new GenericAspectQ();
  }

}
