import java.util.*;

// Checking what gets into the target classes...
//  Here the ITDs are on some target *class*

class C<T> {}

aspect X {

  List C.list1;
  List<Z> C<Z>.list2; 

  String C.field1;
  Q C<Q>.field2;       

}
