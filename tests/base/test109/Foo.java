
public class Foo {
  private              String  _name;
  private static final int PRIVATECONST            = 1;
  private static       int privateClassVar         = 2;
  private              int privateInstanceVar      = 3;

  protected static     int protectedClassVar       = 4;
  protected            int protectedInstanceVar    = 5;
  
  public    static     int publicClassVar          = 6;
  public               int publicInstanceVar       = 7;

  public    static     int ClassVar                = 8;
  public               int InstanceVar             = 9;

  public void foo() {
      //    System.out.println("running " + this + ".foo()");
  }

  private  static void privateClassMethod() {
      //    System.out.println("in " + "Foo.privateClassMethod()");
  }

  private void privateInstanceMethod() {
      //    System.out.println("in " + this + ".privateInstanceMethod()");
  }

  protected  static  void protectedClassMethod() {
      //    System.out.println("in "  + "Foo.protectedClassMethod()");
  }

  protected  void protectedInstanceMethod() {
      //    System.out.println("in " + this + ".protectedInstanceMethod()");
  }

  public  static void publicClassMethod() {
      //    System.out.println("in " + "Foo.publicClassMethod()");
  }

  public  void publicInstanceMethod() {
      //    System.out.println("in " + this + ".publicInstanceMethod()");
  }

  static void ClassMethod() {
      //    System.out.println("in " + "Foo.ClassMethod()");
  }

  void InstanceMethod() {
      //    System.out.println("in " + this + ".InstanceMethod()");
  }


}


