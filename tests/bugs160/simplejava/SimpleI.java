import java.util.*;

interface I {}

class Sub implements I {}

class Sub2 implements I {
  public <N extends Number> int publicMethod(List<N> ns) { return 0; }
}

public aspect SimpleI {
  public static void main(String []argv) { }

  public <N extends Number> int I.publicMethod(List<N> ns) { return ns.size(); }
}


