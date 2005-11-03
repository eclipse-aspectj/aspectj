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
  }
}


interface I1 { }
interface I11  extends I1  { }
interface I111 extends I11 { }
interface I12  extends I1  { }

class C1    implements I1   { C1(String s) {} }
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



aspect A1 {
  static int i1Count, c1Count, c1IntCount = 0;
  // interface
  before(): initialization(I1.new(..)) {
      i1Count++;
  }


  C1.new() {
          c1Count++;
  }
  C1.new(int x) {
          c1IntCount++;
  }
}

aspect Verify {

  // In the current model, introduces on constructors !don't! work their way
  // down the inheritance.  With the given hierarchy, the number of
  // invocations of the introduced constructors should match the
  // numbers below.

     after(): within(Driver) && execution(static void test(..)) {
        Tester.checkEqual(A1.i1Count,    8, "A1.i1Count");
        Tester.checkEqual(A1.c1Count,    2, "A1.c1Count");
        Tester.checkEqual(A1.c1IntCount, 0, "A1.c1IntCount");
    }
}

