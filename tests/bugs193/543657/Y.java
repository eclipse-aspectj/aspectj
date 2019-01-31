import org.aspectj.lang.annotation.*;

@Aspect class Y {

  interface I { 
    public void m2();
  }

  public static class IIimpl implements I {
    public void m2() { System.out.println("Y.IImpl.m2() ran");}
  }

  @DeclareParents(value="Basic3b",defaultImpl=Y.IIimpl.class)
  private I simplefield2;

  @Before("call(* *(..))")
  public void advice1() {}

}
  
