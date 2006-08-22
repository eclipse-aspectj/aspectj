import org.aspectj.lang.annotation.*;
import org.aspectj.lang.*;

@Aspect
class MyAspect {
  @After("staticinitialization(*)")
  public void x(JoinPoint.StaticPart thisJoinPointStaticPart) {
    System.out.println("after initialization "+thisJoinPointStaticPart);
  }
}

public aspect AnnStyle {

  static {
  }

  public static void main(String []argv) {
	    System.out.println("InstanceExists?"+Aspects.hasAspect(MyAspect.class));
  }
}
