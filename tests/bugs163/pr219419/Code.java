import org.aspectj.lang.annotation.*;

public class Code {
  public void emitGooeyMess(String argument) throws Exception {
    throw new RuntimeException("Gooey Mess");
  }

  public static void main(String []argv) {
	  try {
	  new Code().emitGooeyMess("ewwww");
	  } catch (Exception e) {}
  }
}

@Aspect
class TestAspect {
  @Pointcut("execution(* Code.*(..)) && args(s)")
  public void squidStringMethods(String s) {}

  @AfterThrowing(pointcut="squidStringMethods(s)", throwing="e")
  public void catchGooeyMess(Exception e, String s) {
  //public void catchGooeyMess(String s, Exception e) {
    System.out.println("Catching mess. Argument was " + s);
  }
}

