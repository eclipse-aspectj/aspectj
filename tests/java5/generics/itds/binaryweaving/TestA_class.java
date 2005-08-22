import java.util.*;

public class TestA_class {
  public static void main(String []argv) { 
    TestA_generictype<Float> sc = new TestA_generictype<Float>();
    List<Integer> li = new ArrayList<Integer>();
    List<Float> lf = new ArrayList<Float>();
    sc.m(li,lf,li);
  }
}
