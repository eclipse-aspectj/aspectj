public class ConstructorMain { // Bug 61538

  public static void main(String args[]) { 
  	  
	  ConstructorAspects.preinitcalls = 0;
	  B b1 = new B();
	  // preinitcalls should be 2
	  // System.out.println("Preinitcalls="+ConstructorAspects.preinitcalls);
	  int bPreinitcalls = ConstructorAspects.preinitcalls;
	  
      // Only difference between B and C is the order in which the 
      // constructors are declared in the class file
	  ConstructorAspects.preinitcalls = 0;
	  C c1 = new C();
	  // preinitcalls should be 2
	  // System.out.println("Preinitcalls="+ConstructorAspects.preinitcalls);
	  int cPreinitcalls = ConstructorAspects.preinitcalls;
	  
	  if (bPreinitcalls!=cPreinitcalls)
	  	throw new RuntimeException("Both b("+bPreinitcalls+") and c("+cPreinitcalls+") should report the same number of preinit jps");
	}
}

class A {
  int x = 4;
  A (int x) { this.x = x; }
}

class B extends A {
  int y;
  static int k = 4;
  static int j = 5;
  static int l = 6;

  B (int x, int y) { super(x+y); this.y = x+y; }

  B (int x) { this(x+l, x+l); this.y = x+l; }

  B () { this(k+j); this.y = l; }


}
class C extends A {
  int y;
  static int k = 4;
  static int j = 5;
  static int l = 6;

  C () { this(k+j); this.y = l; }

  C (int x) { this(x+l, x+l); this.y = x+l; }

  C (int x, int y) { super(x+y); this.y = x+y; }

}


aspect ConstructorAspects {
	
  public static int preinitcalls = 0;
  public static int initcalls = 0;
  public static int execs = 0;

  static private int aspectnesting = 0;
  private final static boolean log = false;

  static void message(String s) { 
  	if (log) {
  	  for (int i=0; i<aspectnesting; i++) System.out.print("---+");
	  System.out.println(s);
  	}
  }


  // call of all constructors
  pointcut allconstrcalls() :  call(*..new(..)) &&
		   !within(ConstructorAspects) && !call(java.lang..new(..));

  // execution of all constructors
  pointcut allconstrexecutions() : execution(*..new(..)) &&
		   !within(ConstructorAspects);
		   
  // intialization of all constructors
  pointcut allconstrinitializations() : initialization(*..new(..)) &&
		   !within(ConstructorAspects);

  // preinitialization of all constructors
  pointcut allconstrpreinitializations() : preinitialization(*..new(..)) &&
		  !within(ConstructorAspects);

  before(): !within(ConstructorAspects) && allconstrpreinitializations() {
  	preinitcalls++; 
  }
  // before advice
  before () : !within(ConstructorAspects) &&
			  (allconstrpreinitializations()  ||
			  allconstrinitializations() ||
			  allconstrexecutions() ) {
			  message(
				  "BEFORE: " +  thisJoinPointStaticPart.getSourceLocation() +
				  " " +thisJoinPointStaticPart.toLongString());
			  aspectnesting++;
			  }

  // after advice
  after () returning : !within(ConstructorAspects) &&
			  (allconstrpreinitializations() ||
			  allconstrinitializations() ||
			  allconstrexecutions() )
			  {
			  aspectnesting--;
			  message(
				  "AFTER: " +  thisJoinPointStaticPart.getSourceLocation() +
				  " " +thisJoinPointStaticPart.toLongString());
			  }

}
