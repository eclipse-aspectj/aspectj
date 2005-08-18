import java.util.*;
import java.lang.reflect.*;
import org.aspectj.lang.annotation.*;

class I<A> {
  A a;
}

aspect Foo {

//  B I<B>.b;

  C I<C>.m() {
    return x;
  }

  /*
    In something like the above, although 'Z' is clearly a reference to
    a member type variable, it is captured in the 'code' for the method as a
    SingleTypeReference for Z.  It doesnt cause a problem with a 'missing type'
    because the compiler looks up the types in the set of type vars for a 
    member - in this case it will find Z and everything will be ok.
    The problem above is that the type of x is 'A' and yet the return type
    of the method is 'C' - they dont match => BANG

  */
}

public class GenericAspectQ extends I<String> {

  public static void main(String []argv) { 
    GenericAspectQ _instance = new GenericAspectQ();
  }
}
