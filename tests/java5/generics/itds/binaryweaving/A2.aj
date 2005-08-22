import java.util.*;

aspect A2 {
//  declare precedence: A2,A1;

  public List<Z> BaseClass<Z>.list2;

  after(): execution(* run2(..)) {
    BaseClass<Integer> bInt = new BaseClass<Integer>();
    bInt.list2 = new ArrayList<Integer>();
    bInt.count++;
  }

}
