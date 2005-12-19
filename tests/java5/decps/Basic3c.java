import org.aspectj.lang.annotation.*;

public class Basic3c {
  public static void main(String []argv) {
    Basic3c b = new Basic3c();
    if (!(b instanceof X.I)) throw new RuntimeException("Basic3c should implement I");
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

  class IImpl implements I {
    public void m2() { }
    public void m3() { }
    public void m4() { }
  }


  @DeclareParents(value="Basic3c",defaultImpl=IImpl.class)
  private I simplefield;


  @Before("call(* *(..))")
  public void advice1() {}

}
  
