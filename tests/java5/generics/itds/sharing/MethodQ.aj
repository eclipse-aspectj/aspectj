import java.util.*;

public class MethodQ {
  public static void main(String []argv) { 
    SimpleClass<Float> sc = new SimpleClass<Float>();
    List<Integer> li = new ArrayList<Integer>();
    List<Float> lf = new ArrayList<Float>();
    sc.m(li,lf,li);
  }
}

class SimpleClass<N> {// extends Number> { 
 // This is what we are trying to mimic with our ITD
 //public <L extends Number> void m(List<L> ll1, List<N> lz,List<L> ll2) {} 
}

aspect X {
  // scary, multiple tvars, one from member, one from target
  public <L extends Number> void SimpleClass<Z>.m(List<L> ll1, List<Z> lz,List<L> ll2) {} 
}
