import java.util.*;

class Victim {}

public class BizarroSignatures {
  public static void main(String []argv) {
    Victim v = new Victim();
    
    Map<Double,Double> m = new HashMap<Double,Double>();
    v.a(m);
    
    List<A> as = new ArrayList<A>();
    v.b(as);
    v.c(as);

    //v.c(as);

  }
 
}

class A implements Comparable<A> { 
  public int compareTo(A a) { return 0; }
}

aspect X { 

  public void Victim.a(Map<?,? extends Number> map) {}

  public <T extends Object & Comparable<? super T>> 
    void Victim.b(List<T> l) {}

  public <P extends Object & Comparable<? super P>> 
    void Victim.c(List<P> col) {};//return null;}
  
//  public <T extends Comparable<? super Number>> 
//    T Victim.d(Collection<T> col) {return null;}
//
//  public <T extends Comparable<T>> 
//    T Victim.e(Collection<T> col) {return null;}
//
//  public <X> 
//    X Victim.f(Collection<X> x) {return null;}
//
//  public void Victim.g(List<List<List<List<List<? extends List>>>>> wtf) {}
// 
//  public <T> 
//    void Victim.h(List <T> a,List<? extends T> b) {}
//
//  public <T extends Number> 
//    void Victim.i(Map<T,? super Number> n) {}

//  public <T> 
//    void Victim.j(T[] ts,Collection<T> c) {}
}
