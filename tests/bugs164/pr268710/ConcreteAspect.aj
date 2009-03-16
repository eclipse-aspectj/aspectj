/**
 * 
 */
package none;

/**
 * @author Dawid Pytel
 *
 */
public aspect ConcreteAspect extends GenericAspect<String> {

  public static void main(String [] argv) {
    new C();
  }


  before(SomeInterface v): SomeConstructor(v) {
    System.out.println("Building an object "+v.getClass());
  }
}

class C implements GenericAspect.SomeInterface {
  public C() {
    System.out.println("C.init");
  }
}
