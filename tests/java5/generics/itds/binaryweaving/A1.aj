import java.util.*;

aspect A1 {

  public List<String> BaseClass.list1;

  after(BaseClass c): execution(* run1(..)) && this(c) {
    List<String> myLs = new ArrayList<String>();
    c.list1 = myLs;
    c.count++;
  }

}
