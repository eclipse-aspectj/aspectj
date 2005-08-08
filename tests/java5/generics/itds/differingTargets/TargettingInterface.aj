import java.util.*;

interface I {}

class Sub implements I {}

class Sub2 implements I {

  public <N extends Number> int publicMethod(List<N> ns) {
    return 0;
  }
}

public aspect TargettingInterface {
  public static void main(String []argv) {
    List<Number> ns = new ArrayList<Number>();
    ns.add(5); ns.add(2); ns.add(3);

    int total = 0; 

    Sub s = new Sub();
    total+=s.defaultMethod(ns);
    total+=s.publicMethod(ns);
    total+=s.privateMethod(ns);
    if (total!=9) throw new RuntimeException("Should be 9 but is "+total);

    total = 0;
    Sub2 s2 = new Sub2();
    total+=s2.defaultMethod(ns);
    total+=s2.publicMethod(ns);
    total+=s2.privateMethod(ns);
    if (total!=6) throw new RuntimeException("Should be 6 but is "+total);
  }

  <N extends Number> int I.defaultMethod(List<N> ns) {
    return ns.size();
  }

  public <N extends Number> int I.publicMethod(List<N> ns) {
    return ns.size();
  }

  public <N extends Number> int I.privateMethod(List<N> ns) {
    return ns.size();
  }
}

