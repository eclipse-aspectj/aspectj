import java.util.*;

// Checking what gets into the target classes...
//  Here the ITDs are on some interface and so found in the class
//  that implements the interface

class C implements I<String> {}

interface I<T> {}

aspect X {

  List<Z> I<Z>.ln; 

  Q I<Q>.n; 

}
