import org.aspectj.lang.annotation.*;

public class Basic1b {
  public static void main(String []argv) {
    Basic1b b = new Basic1b();
    if (!(b instanceof X.I)) throw new RuntimeException("Basic1b should implement I");
  }
}



@Aspect
class X {

  interface I { 
  }
 
  @DeclareParents("Basic1b")
  private I someField;

}
  
