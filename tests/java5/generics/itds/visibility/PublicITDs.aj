import java.util.*;

class Base { 

}

public class PublicITDs {

  public static void main(String[] argv) {
    List<Double> l1 = new ArrayList<Double>();
    Base b = new Base();
    b.publicMethod1(l1);           
    b.publicMethod2(l1,l1);         
    Base b2 = new Base(l1);
    Base b3 = new Base(l1,l1);
    Map<Integer,Double> m1 = new HashMap<Integer,Double>();
    Base b4 = new Base(l1,m1);
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

  // what use is this next one??
  // public <R extends Number>    R Base.publicMethod3() { return null;}
}
