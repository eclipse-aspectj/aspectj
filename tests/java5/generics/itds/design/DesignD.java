import java.util.*;

class C<N extends Number> {}

interface I {}

aspect X {

  void C<R>.m0(R n) { }   // L9

  List<Q> C<Q>.m0(Q q,int i,List<List<Q>> qs) {return null;} // L11

}
