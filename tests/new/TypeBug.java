class TypeBug {

  public static void main(String[] args) {
    new TypeBug().go();
  }

    A a = new A();
    D d = new D();
    G g = new G();

  void go(){
    g.foo(a);
    g.foo(d);
    bar(g);
    bash(g);
  }

  void bar(I i){
    i.foo(a);
  }

  void bash(J j){
    j.foo(d);
  }

}

aspect Q {
 
  pointcut pc1(): receptions(void I.foo(*));
  pointcut pc2(): calls(void I.foo(*));
  pointcut pc3(): callsto(pc1());

  pointcut test():;

  static before(): pc1() {
    System.out.print("(pc1) ");
  }
 

  static before (): pc2() {
    System.out.print("(pc2) ");
  }

  static before(): pc3(){
    System.out.print("(pc3) ");
  }

}


class A {}


class D {}


interface I {
  void foo(A a);
}

interface J {
  void foo(D d);
}

class G implements I, J {
  public void foo(A a){
    System.out.println("G.foo(A)");
  }
  public void foo(D d){
    System.out.println("G.foo(D)");
  }
}
