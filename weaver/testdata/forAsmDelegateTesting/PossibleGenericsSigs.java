import java.util.*;

public class PossibleGenericsSigs {

  public void a(List<String> List_String) {}

  public void b(List<Double> List_Double) {}

  public void c(List<? extends Number> q_extends_Number) {}

  public void d(List<? super Number> q_super_Number) {}

  public void e(List<?> List_q) {}

  public void f(Map<?,? super Number> Map_q_q_super_Number) {}



  <T extends Object & Comparable<? super T>> void r(List<T> l) {}

  <T extends Object & Comparable<? super T>> T s(Collection<T> col) {return null;}
  
  static <T extends Comparable<? super Number>> T t(Collection<T> col) {return null;}

  static <T extends Comparable<T>> T u(Collection<T> col) {return null;}

  <X> X v(Collection<X> x) {return null;}

  public void w(List<List<List<List<List<? extends List>>>>> wtf) {}
 
  static <T> void x(List <T> a,List<? extends T> b) {}

  <T extends Number> void y(Map<T,? super Number> n) {}

  static <T> void z(T[] ts,Collection<T> c) {}
}
