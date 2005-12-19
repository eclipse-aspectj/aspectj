import org.aspectj.lang.annotation.*;

public class Basic3b {
  public static void main(String []argv) {
    Basic3b b = new Basic3b();
    if (!(b instanceof X.I)) throw new RuntimeException("Basic3b should implement I");
    ((X.I)b).m2();
    ((X.I)b).m3();
    ((X.I)b).m2();
    ((X.I)b).m4();
  }
}



@Aspect class X {

  interface I { 
    public void m2();
    public void m3();
    public void m4();
  }

  public static class IIimpl implements I {
    public void m2() { }
    public void m3() { }
    public void m4() { }
  }


  @DeclareParents(value="Basic3b",defaultImpl=X.IIimpl.class)
  private I simplefield;

  @Before("call(* *(..))")
  public void advice1() {}

}
  
