import org.aspectj.lang.annotation.*;

public class SimpleN {
  public static void main(String[]argv) {
   new A().m(1,2,3);
  }
}

class A {
  public void m(int p,int q,int r) {
    String s= "Hello";
    if (s.equals("five")) {
      int i = 5;
      System.out.println("Je suis ici "+i);
      i = i + 6;
      if (i==3) {
        String p2 = "foo";
      }
    } else {
      int j = 6;
      System.out.println("Ich bin hier "+j);
    }
    System.out.println("Finished "+s);
  }
}

aspect X {
  pointcut complicatedPointcut(): execution(* nonExistent(..)) && if(true==true);

  @SuppressAjWarnings("adviceDidNotMatch")
  before(): complicatedPointcut() {
    String s= "Hello";
    if (s.equals("five")) {
      int i = 5;
      System.out.println("Je suis ici "+i);
      i = i + 6;
      if (i==3) {
        String p2 = "foo";
      }
    } else {
      int j = 6;
      System.out.println("Ich bin hier "+j);
    }
    System.out.println("Finished "+s);
  }
}
