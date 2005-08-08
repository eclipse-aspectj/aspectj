import java.util.*;

class Class {}

public aspect TargettingClass {
  public static void main(String []argv) {
    List<Number> ns = new ArrayList<Number>();
    ns.add(5); ns.add(2); ns.add(3);

    Class s = new Class();
    int total = 0;
    total+=s.defaultMethod(ns);
    total+=s.publicMethod(ns);
    total+=s.privateMethod(ns);
    if (total!=9) throw new RuntimeException("Should be 9 but is "+total);
  }

  <N extends Number> int Class.defaultMethod(List<N> ns) {
    return ns.size();
  }

  public <N extends Number> int Class.publicMethod(List<N> ns) {
    return ns.size();
  }

  public <N extends Number> int Class.privateMethod(List<N> ns) {
    return ns.size();
  }
}

