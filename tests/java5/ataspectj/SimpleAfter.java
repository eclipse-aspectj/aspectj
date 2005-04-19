import org.aspectj.lang.annotation.*;

public class SimpleAfter {

  public static void main(String []argv) {
    SimpleAfter instance = new SimpleAfter();
    X.s.append("1");
    instance.m();
    if (!X.s.toString().equals("12a")) 
      throw new RuntimeException("Either advice not run or ordering wrong, expected 12a: "+X.s);
  }

  public void m() {
  	X.s.append("2");
  }


    @Aspect()
    public static class X {

        public static StringBuffer s = new StringBuffer("");

        @After("execution(* SimpleAfter.m())")
        public void before() {
          s.append("a");
        }
    }

}

