import java.util.*;

class Base { 

}

public class PublicITDsErrors {

  public static void main(String[] argv) {
    List<Double> l1 = new ArrayList<Double>();
    List<Float> l2 = new ArrayList<Float>();
    Base b = new Base();
    b.publicMethod2(l1,l2); // CE attempt to bind tvar to Float & Double
    Map<Integer,String> m1 = new HashMap<Integer,String>();
    Base b4 = new Base(l1,m1); // CE attempt to bind tvarP to Double and String
  }

}

aspect X {
  // methods
  public <R extends Number> void Base.publicMethod1(List<R> lr) {}
  public <R extends Number> void Base.publicMethod2(List<R> lr1,List<R> lr2) {}

  // ctor
  public <P extends Number> Base.new(List<P> lr) { this(); }
  public <P extends Number> Base.new(List<P> lr1,List<P> lr2) { this(); }
  public <P,Q extends Number> Base.new(List<P> lp,Map<Q,P> m1) { this(); }
}
