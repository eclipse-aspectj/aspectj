
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
  }

  { publicA = 6; }
}

aspect A {
  int pkg1.Bar.interTypeField = 0;
  //void Good.interTypeMethod() { } 

  int j;

  before(): execution(void Good.foo()) {
    System.out.println("");
  }

  public void m() { } 

  pointcut all(): call(* *(..));

  after(): all() { System.out.println(""); }

  declare warning: call(* mumble*(..)): "warning";
  declare error: call(* gumble*(..)): "error";  
//  declare parents: Point extends java.io.Serializable;
//  declare parents: Point implements java.util.Observable;
//  declare soft: Point: call(* *(..));
}

interface I { }


//privileged aspect PrivilegedAspect { }

 

