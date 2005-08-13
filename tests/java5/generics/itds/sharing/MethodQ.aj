import java.util.*;

public class MethodQ {
  public static void main(String []argv) { 
	  SimpleClass<Float> sc = new SimpleClass<Float>();
	  sc.m<Integer>(new ArrayList<Integer>(),new ArrayList<Float>());
  }
}

class SimpleClass<N extends Number> { }

aspect X {
  public <L extends Number> void SimpleClass<Z>.m(List<L> ll, List<Z> lz) {} // scary, multiple tvars, one from member, one from target
}
