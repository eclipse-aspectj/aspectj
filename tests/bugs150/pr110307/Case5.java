import java.util.*;
import java.lang.annotation.*;
import java.lang.ref.*;

interface I<T> {
}

class A {
  // error, not compatible with List<String> from supertype
  List<Integer> foo() { return null; } 
}

aspect X {

  List<T> I<T>.foo() { return null; }

  declare parents: A implements I<String>;
}
