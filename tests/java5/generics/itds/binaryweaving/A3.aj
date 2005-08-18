import java.util.*;

aspect A3 {

  public List<Z> BaseClass<Z>.m(List<Z> lz) {
    return lz;
  }

  after(BaseClass c): execution(* run1(..)) && this(c) {
    List<String> myLs = new ArrayList<String>();
    BaseClass<String> bStr = new BaseClass<String>();
    List<String> ls2 = bStr.m(myLs);
    System.err.println("Advice from A3 ran successfully");
  }

}
