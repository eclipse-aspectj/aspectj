import java.util.*;

class C<N extends Number> {}

interface I {}

aspect X {

  List<Z> C<Z>.ln; // L9

  Q C<Q>.n;        // L11

}
