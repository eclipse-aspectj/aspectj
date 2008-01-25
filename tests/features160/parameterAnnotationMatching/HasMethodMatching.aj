import java.io.*;

// Should match as indicated
public aspect HasMethodMatching {

  declare parents: hasmethod(* a(@Anno1 *)) implements Serializable; // yes Target1
  declare parents: hasmethod(* b(@Anno1 *)) implements Serializable; // no
  declare parents: hasmethod(* c(@Anno1 (*))) implements Serializable; // yes Target3
  declare parents: hasmethod(* d(@Anno1 (@Anno2 *))) implements Serializable; // yes Target4
  declare parents: hasmethod(* e(@Anno1 (@Anno2 *))) implements Serializable; // no

  public static void main(String []argv) {
    System.out.println("Target1? "+(new Target1() instanceof Serializable));
    System.out.println("Target2? "+(new Target2() instanceof Serializable));
    System.out.println("Target3? "+(new Target3() instanceof Serializable));
    System.out.println("Target4? "+(new Target4() instanceof Serializable));
    System.out.println("Target5? "+(new Target5() instanceof Serializable));
  }
}

class Target1 {
  public void a(AnnotatedWithAnno1 p) {}
}

class Target2 {
  public void b(@Anno1 String p) {}
}

class Target3 {
  public void c(@Anno1 String p) {}
}

class Target4 {
  public void d(@Anno1 AnnotatedWithAnno2 p) {}
}

class Target5 {
  public void e(@Anno1 AnnotatedWithAnno1 p) {}
}
