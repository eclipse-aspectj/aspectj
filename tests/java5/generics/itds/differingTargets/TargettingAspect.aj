import java.util.*;

public aspect TargettingAspect {
  public static void main(String []argv) {
    List<Number> ns = new ArrayList<Number>();
    ns.add(5); ns.add(2); ns.add(3);

    TargettingAspect s = TargettingAspect.aspectOf();
    int total = 0 ;
    total+=s.defaultMethod(ns);
    total+=s.publicMethod(ns);
    total+=s.privateMethod(ns);
    if (total!=9) throw new RuntimeException("Result should be 9 but is "+total);
  }

  <N extends Number> int TargettingAspect.defaultMethod(List<N> ns) {
    return ns.size();
  }

  public <N extends Number> int TargettingAspect.publicMethod(List<N> ns) {
    return ns.size();
  }

  public <N extends Number> int TargettingAspect.privateMethod(List<N> ns) {
    return ns.size();
  }
}

