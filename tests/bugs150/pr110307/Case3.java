import java.util.*;
import java.lang.annotation.*;
import java.lang.ref.*;

interface I<T> {
}

class A {
}

aspect X {

  List<T> I<T>.foo() { return null; }  // should be ok...

  declare parents: A implements I<String>;
}
