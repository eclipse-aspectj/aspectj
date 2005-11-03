import java.util.*;

class Base { 

}

public aspect PackageITDs {

  public static void main(String[] argv) {
    List<Double> l1 = new ArrayList<Double>();
    Base b = new Base();
    b.packageMethod1(l1);           
    b.packageMethod2(l1,l1);         
    Base b2 = new Base(l1);
    Base b3 = new Base(l1,l1);
    Map<Integer,Double> m1 = new HashMap<Integer,Double>();
    Base b4 = new Base(l1,m1);
  }


  // methods
  <R extends Number> void Base.packageMethod1(List<R> lr) {}
  <R extends Number> void Base.packageMethod2(List<R> lr1,List<R> lr2) {}

  // ctor
  <P extends Number> Base.new(List<P> lr) { this(); }
  <P extends Number> Base.new(List<P> lr1,List<P> lr2) { this(); }
  <P,Q extends Number> Base.new(List<P> lp,Map<Q,P> m1) { this(); }

}
