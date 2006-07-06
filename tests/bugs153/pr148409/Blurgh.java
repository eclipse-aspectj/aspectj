

interface SecurityEntity {}
class Goo {}
interface Base {}
interface Hoo<X> { public void m(X x);}

class Foo<B extends Base, C extends B> extends Goo implements Hoo<B> {

  public void m(B b) {
  }
}


interface Interface1 extends Base {}

class Impl1 implements Interface1 {}




public class Blurgh {
  public static void main(String []argv) {
    new Foo<Interface1,Impl1>();
   }
}

