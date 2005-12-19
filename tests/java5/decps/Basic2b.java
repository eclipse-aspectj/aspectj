import org.aspectj.lang.annotation.*;

public class Basic2b {
  public static void main(String []argv) {
    Basic2b b = new Basic2b();
    if (!(b instanceof X.I)) throw new RuntimeException("Basic2b should implement I");
  }
}



@Aspect class X {

  interface I { 
  }

  public static class IIimpl implements I {
    public void m2() { }
  }


  @DeclareParents(value="Basic2b",defaultImpl=X.IIimpl.class)
  private I simplefield;


  @Before("execution(* *(..))")
  public void advice1() {}

}
  
