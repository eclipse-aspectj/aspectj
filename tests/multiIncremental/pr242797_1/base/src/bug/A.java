import java.util.*;

interface  A {
 
  List<?> getfoos() ;
}

aspect X {
  List<?> A.getFoos() { return null; }
}
