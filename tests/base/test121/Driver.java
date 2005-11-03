import org.aspectj.testing.Tester;
public class Driver {
  
  public static void main(String[] args) { test(); }

  public static void test() {
    Foo.staticMethod();
    Foo.introducedStaticMethod();

    Foo foo = new Foo(10);

    foo.nonStaticMethod();
    foo.introducedNonStaticMethod();

      Tester.checkEqual(A.fooStaticCounter, 1, "A.fooStaticCounter");
      Tester.checkEqual(A.fooCounter, 1, "A.fooCounter");
      Tester.checkEqual(A.aStaticCounter, 1, "A.aStaticCounter");
      Tester.checkEqual(A.aCounter, 1, "A.aCounter");
      // these is only one constructor call, for Foo
      Tester.checkEqual(A.constructorCounter, 1, "constructor calls");
      // one for Foo, one for A
      Tester.checkEqual(A.initializationCounter, 2, "initializations");
      Tester.check(A.ranIntroducedConstructor, 
                    "no overriding of the real thing");
  }
}

class Foo { 
  
  static void staticMethod() { }
         void nonStaticMethod() { }
}

aspect A0_8beta1 {
    after() returning(): /*target(*) &&*/ call(new(int)) {
        A.constructorCounter++;
    }
    after() returning(): /*target(*) &&*/ initialization(new(..)) && !within(A0_8beta1) {
    	System.out.println("init at " + thisJoinPoint);
        A.initializationCounter++;
    }
    
    before(): within(Foo) && execution(static * Foo.*(..)) {
        A.fooStaticCounter++;
    }
    
    before(): within(A) && execution(static * Foo.*(..)) {
        A.aStaticCounter++;
    }
    
    before(): within(A) && execution(!static * Foo.*(..)) {
        A.aCounter++;
        System.out.println("external before advise on " + thisJoinPoint);
    }
}

aspect A pertarget(target(Foo)){
  
    static int constructorCounter = 0;
    static int initializationCounter = 0;
    static int aStaticCounter = 0;
    static int aCounter = 0;
    static int fooStaticCounter = 0;
    static int fooCounter = 0;

    static boolean ranIntroducedConstructor = false;

    //introduction Foo {
    static void Foo.introducedStaticMethod() {
        // System.out.println(thisJoinPoint.className +"."+ 
        // thisJoinPoint.methodName);
    }
    void Foo.introducedNonStaticMethod() {
        // System.out.println(thisJoinPoint.className +"."+ 
        // thisJoinPoint.methodName);
    }
    Foo.new(int n) { this(); ranIntroducedConstructor = true; }

	// make sure advice doesn't go on the toString() method
	// this would result in an infinite recursion
	before(): within(Foo) && execution(!static * Foo.*(..)) {
	    fooCounter++;
	    //System.out.println("before advise on " + 
	    //thisJoinPoint.className +"."+ thisJoinPoint.methodName);
	}
	
	public A() { System.err.println("creating: " + this); }

    //XXX moved to other aspect, need to think about this...
    //before(): within(A) && executions(!static * Foo.*(..)) {
    //aCounter++;
    //System.out.println("before advise on " + thisJoinPoint);
    //}
}

