import org.aspectj.lang.annotation.*;

public class SimpleBefore {

  public static void main(String []argv) {
    SimpleBefore instance = new SimpleBefore();
    X.s.append("1");
    instance.m();
    if (!X.s.toString().equals("1b2")) 
      throw new RuntimeException("Either advice not run or ordering wrong, expected 1b2: "+X.s);
  }

  public void m() {
  	X.s.append("2");
  }
}

@Aspect("issingleton")
class X {
	
	public static StringBuffer s = new StringBuffer("");

    @Before("execution(* SimpleBefore.m())")
    public void before() {
      s.append("b");
    }
}
