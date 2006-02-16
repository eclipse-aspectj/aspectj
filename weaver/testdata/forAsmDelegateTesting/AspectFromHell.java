import java.util.*;

public aspect AspectFromHell {

  public void Foo.m1() {}
  public int Foo.m2() {return 2;}
  public void Foo.m3(String s) {}
  public Foo.new(String s) {super();}
  public int Foo.x;
  public List Foo.y;


  before(): execution(void Goo.m1()) {}
  after(): execution(void Goo.m2(String)) { System.err.println(thisJoinPoint);}
  void around(int i): execution(void Goo.m3(..)) && args(i) { }

  class Goo {
    void m1() {}
    void m2(String s) {}
    void m3(int i) {}
  }
}

  class Foo { }
