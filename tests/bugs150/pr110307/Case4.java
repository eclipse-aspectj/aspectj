import java.util.*;
import java.lang.annotation.*;
import java.lang.ref.*;

interface I<T> {
}

class A {
  List<String> foo() { return null; }
}

aspect X {

  List<T> I<T>.foo() { return null; }  // should be ok - A implements I<String>

  declare parents: A implements I<String>;
}
