//!!! ugly test case

public class HierarchyIntroductions {
    public static void main(String[] args) { test(); } 

    static public void test() {
	C1 c1 = new C1();
	I1 i1 = c1;
	C2 c2 = new C2();
	c2.foo();
	c1 = c2;
	I4 i4 = c2;
	I2 i2 = c1;
    }
}


aspect Hierarchy {
    //introduction C1 {
    declare parents: C1 implements I1;
    declare parents: C1 implements I2;
    //}

    //introduction C2 {
    declare parents: C2 extends C1;
    //}

    //introduction C4 {
    declare parents: C4 extends C3;
    //}
    //introduction C5 {
    declare parents: C5 extends C3a;
    //}

    //introduction I2 {
    declare parents: I2 extends I3, I4, I5;
    //}
}


class C1 { public void foo() { } }
class C2 {}
class C3 {}
class C3a extends C3 {}
class C4 extends C3 {}
class C5 extends C3 {}

interface I1 {}
interface I2 {}
interface I3 {}
interface I4 {}
interface I5 {}
