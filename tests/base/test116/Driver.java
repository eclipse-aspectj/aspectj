import org.aspectj.testing.Tester;

public class Driver {
  public static void main(String[] args) { test(); }

  public static void test() {
    Class c = new Class();
    
    c.l();
    c.l(2);
    c.m(3);

    Tester.checkEqual(Aspect.c1, 3, "Aspect.c1");
    Tester.checkEqual(Aspect.c2, 2, "Aspect.c2");
    Tester.checkEqual(Aspect.c3, 1, "Aspect.c3");
    Tester.checkEqual(Aspect.c4, 2, "Aspect.c4");
  }
}

class Class {
  
  public void l()      throws Error {
      //System.out.println("public void l()    throws Error");
    //throw(new Error ());
  }

  void l(int x)              {
      //System.out.println("       void l(int)");
  }

  public void m(int x) throws Error {
      //System.out.println("public void m(int) throws Error");
    // throw(new Error ());
  }

}

aspect Aspect {
  static int c1, c2, c3, c4 = 0;

   before(): target(Class) && call(void *(..)) { 
      //System.out.println("before Class.*(..)"); 
      c1++;
  }

   before(int x): target(Class) && call(void *(int))&& args(x) { 
      //System.out.println("before Class.*(int)"); 
      c2++;
  }

   before(int x): target(Class) && call(public void *(int)) && args(x){ 
      //System.out.println("before public Class.*(int)"); 
      c3++;
  }

   before(): target(Class) && call(void *(..) throws Error) { 
      //System.out.println("before Class.*(..)  throws Error"); 
      c4++;
  }
}
