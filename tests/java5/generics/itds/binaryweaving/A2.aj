import java.util.*;

aspect A2 {

  public List<Z> BaseClass<Z>.list2;

  after(): execution(* run2(..)) {
    BaseClass<Integer> bInt = new BaseClass<Integer>();
    bInt.list2 = new ArrayList<Integer>();
    System.err.println("Advice from A2 ran successfully");
  }

}
