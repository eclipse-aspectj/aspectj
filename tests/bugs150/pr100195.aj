import org.aspectj.testing.Tester;

public class pr100195 {
    public static void main(String[] args) {
        new Foo().foo();
        AroundCasting.main(new String[0]);
    }	
}

class Foo {
	
    static int x;
	
    private String myString = "A String";
 
    public static void main(String[] args) {
        new Foo().foo();
        AroundCasting.main(new String[0]);
    }
 
    public void foo() {
      String myLocal = myString;
      x = 5;
      System.out.println(myLocal);   // breakpoint here
      bar(x);
    }
    
    public void bar(int y) {}
}
// Test.aj
aspect Test {
  void around() : ( execution(* Foo.foo(..) ) ) {
	  int y = 4;
      System.out.println("before");
      proceed();
      System.out.println("after");
  }
}

class AroundCasting {
    public static void main(String[] args) {
    	bar(x);
        //Tester.checkEqual(x, 1003);
    }
    static int x;
    
    static void bar(int y) {}
}


aspect A {
    static boolean test() { return true; }

    int around(): if (test()) && get(int AroundCasting.x) {
        return proceed() + 1000;
    }

    void around(): execution(void AroundCasting.main(String[])) {
        Tester.event("enter main");
        proceed();
    }
}
