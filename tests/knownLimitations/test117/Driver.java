import org.aspectj.testing.Tester;

public class Driver {
  public static void main(String[] args) { test(); }

  public static void test() {
    C1       c1       = new C1();
    C11      c11      = new C11();  
    C111     c111     = new C111();
    C12      c12      = new C12();  
    Cleaf1   cleaf1   = new Cleaf1();
    Cleaf11  cleaf11  = new Cleaf11();
    Cleaf111 cleaf111 = new Cleaf111();
    Cleaf12  cleaf12  = new Cleaf12();

    Tester.checkEqual(c1.a, 0, "c1.a");
    Tester.checkEqual(c11.a, 0, "c11.a");
    Tester.checkEqual(c111.a, 0, "c111.a");
    Tester.checkEqual(c12.a, 0, "c12.a");

    Tester.checkEqual(cleaf1.a, 0, "cleaf1.a");
    Tester.checkEqual(cleaf11.a, 0, "cleaf11.a");
    Tester.checkEqual(cleaf111.a, 0, "cleaf111.a");
    Tester.checkEqual(cleaf12.a, 0, "cleaf12.a");

    Tester.checkEqual(c1.b, 0, "c1.b");
    Tester.checkEqual(cleaf1.b, 0, "cleaf1.b");

    Tester.checkEqual(I1.c, 5, "I1.c");

    Tester.checkEqual(c1.d, 1, "c1.d");
    Tester.checkEqual(c11.d, 1, "c11.d");
    Tester.checkEqual(c111.d, 1, "c111.d");
    Tester.checkEqual(c12.d, 1, "c12.d");

    Tester.checkEqual(c1.e, 2, "c1.e");
    Tester.checkEqual(cleaf1.e, 2, "cleaf1.e");

    Tester.checkEqual(C1.f, 4, "C1.f");
    Tester.checkEqual(cleaf1.f, 4, "cleaf1.f");
    Tester.checkEqual(c1.f, 4, "c1.f");

    Tester.checkEqual(c1.getF(), 4, "c1.getF()");
  }
}

interface I1 { }
interface I11  extends I1  { }
interface I111 extends I11 { }
interface I12  extends I1  { }

class C1    implements I1   { }
class C11   implements I11  { } 
class C111  implements I111 { }
class C12   implements I12  { }

class Cleaf1   extends C1   { }
class Cleaf11  extends C11  { }
class Cleaf111 extends C111 { }
class Cleaf12  extends C12  { }

// For this class structure:  here is the "directly implements" relation
// C1    directly implements I1
// C11   directly implements I11
// C11   directly implements I1     
// C111  directly implements I111
// C111  directly implements I11    
// C111  directly implements I1     
// C12   directly implements I12
// C12   directly implements I1     



// introducing a bunch of variables (almost all non-static).
aspect A1 {

  // Variable Introductions:
  // without initializer   X   designator is interface
  // with    initializer   X   designator is class

  // with initializer, interface also has the two cases of presence or
  // absence of the modifiers.

  // introduce on initializer without initializer
  // should introduce into:
  // C1, C11, C111, C12
    int I1.a;

  // introduce on interface with initializer but no  Mods
  // should introduce into:
  // C1, C11, C111, C12
    int I1.d = 1;

  // introduce on interface with initializer and Mods
  // should introduce into:
  // I1
    public static final int I1.c = 5;

   // introduce on class 
   // should introduce into
   // C1
    int C1.b;
    int C1.e = 2;

  // introduce on class with static modifier
  // should introduce into
  // C1
    static int C1.f = 2;

    int C1.getF() { return 2; }
}

aspect A2 {
	declare precedence: A2, A1;
	
    int around() : set(int C1.f) && within(A1) {
	return C1.f = 3;
    }

    after (): staticinitialization(C1) {
	C1.f = 4;
    }

    // this should override the introduction from A1
    int C1.getF() { return 4; }
}
