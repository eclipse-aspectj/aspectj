// Does annotating 'new' with a type work as desired?

import org.aspectj.testing.Tester;

public class ConstructorSignatures {
    public static void main(String[] args) {
	new A1(); 
	Tester.checkEqual(A.a0, 2, "A0 advice overcalled");
	Tester.checkEqual(A.a1, 1, "A1 advice overcalled");
	Tester.checkEqual(A.b,  0, "B advice overcalled");
	A.a0 = A.a1 = A.b = 0;

	new B();
	Tester.checkEqual(A.a0, 0, "-A0 advice overcalled");
	Tester.checkEqual(A.a1, 0, "-A1 advice overcalled");
	Tester.checkEqual(A.b,  1, "-B advice overcalled");
    }
}

class A0 { }

class A1 extends A0 { }
    
class B {}

aspect A {
    static int a0, a1, b;
    /*static*/ before(): execution(A0+.new()) {  //added +
	a0++;
    }

    /*static*/ before(): execution(A1.new()) {  
	a1++;
    }

    /*static*/ before(): execution(B.new()) {  
	b++;
    }
}
