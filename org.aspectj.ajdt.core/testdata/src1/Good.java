
import java.util.*;
import java.io.IOException;

public class Good {

  public static String foo;
  public int publicA = 1;
  private int privateA = 2;
  protected int protectedA = 3;
  int packageA = 4;

  { publicA = 5; }

  static { foo = "hi"; }

  public Good() { } 

  public void foo() {
    int i = 0;
    i += 1;
    i += 2;
    
    int aspect = 2;
    
    System.out.println(1 + aspect +", "  + aspect++);
  }

  { publicA = 6; }
  
  
  static aspect x { }
}

aspect A {
	public void m() { }
}

/*

aspect A {
  int j;

  public void m() { } 

  pointcut all(): call(* *(..));

  after all(): { System.out.println(""); }

  before(): call(* *(..)) {
    System.out.println("");
  }
}

interface I { }
*/

//privileged aspect PrivilegedAspect { }

 

