// This test verifies that join points exist in the execution of field initializers. 

import org.aspectj.testing.Tester;

//class Tester {
//	public static void checkEqual(int i1, int i2, String s) {
//		System.err.println(s + ": " + i1 + " == " + i2);
//	}
//}

public class FieldInitializerJoinPoints {
    
    static int jcount = 0;
    static int kcount = 0;
    static int lcount = 0;
    static int mcount = 0;

    static int ncount = 0;
    static int ocount = 0;
    static int pcount = 0;
    static int qcount = 0;
    
    static int nMcount = 0;
    static int oMcount = 0;
    static int pMcount = 0;
    static int qMcount = 0;    

    static final int j = 99; // not even a set join point here. 
    static int k = 98;       // a set join point
    final int l = 97;        // not a join point (constant final)
    int m = 96;              // yet another set join point  

	void foo() {
		int i = 97;
		switch(i) {
			case l: System.err.println(l);
		}
	}

    static int n() { return 95; }
    static int o() { return 94; }
    static int p() { return 93; }
    static int q() { return 92; }

    static final int n = n(); // a call and set join point
    static int o = o();       // a call and set join point
    final int p = p();        // a call and set join point
    int q = q();              // a call and set join point

    public static void main(String[] args) {
	new FieldInitializerJoinPoints();
	new FieldInitializerJoinPoints();	

	Tester.checkEqual(jcount, 0, "jcount");
	Tester.checkEqual(kcount, 1, "kcount");
	Tester.checkEqual(lcount, 0, "lcount");
	Tester.checkEqual(mcount, 2, "mcount");

	Tester.checkEqual(ncount, 1, "ncount");
	Tester.checkEqual(ocount, 1, "ocount");
	Tester.checkEqual(pcount, 2, "pcount");
	Tester.checkEqual(qcount, 2, "qcount");	
	
	Tester.checkEqual(nMcount, 1, "nMcount");
	Tester.checkEqual(oMcount, 1, "oMcount");
	Tester.checkEqual(pMcount, 2, "pMcount");
	Tester.checkEqual(qMcount, 2, "qMcount");
    }

}




aspect A {
	public static void mumble(FieldInitializerJoinPoints fp) {
		System.err.println(fp.l);
	}
	
    before(): set(int FieldInitializerJoinPoints.j) { FieldInitializerJoinPoints.jcount++; }
    before(): set(int FieldInitializerJoinPoints.k) { FieldInitializerJoinPoints.kcount++; }
    before(): set(int FieldInitializerJoinPoints.l) { FieldInitializerJoinPoints.lcount++; }
    before(): set(int FieldInitializerJoinPoints.m) { FieldInitializerJoinPoints.mcount++; }

    before(): set(int FieldInitializerJoinPoints.n) { FieldInitializerJoinPoints.ncount++; }
    before(): set(int FieldInitializerJoinPoints.o) { FieldInitializerJoinPoints.ocount++; }
    before(): set(int FieldInitializerJoinPoints.p) { FieldInitializerJoinPoints.pcount++; }
    before(): set(int FieldInitializerJoinPoints.q) { FieldInitializerJoinPoints.qcount++; }

    before(): call(int FieldInitializerJoinPoints.n()) { FieldInitializerJoinPoints.nMcount++; }
    before(): call(int FieldInitializerJoinPoints.o()) { FieldInitializerJoinPoints.oMcount++; }
    before(): call(int FieldInitializerJoinPoints.p()) { FieldInitializerJoinPoints.pMcount++; }
    before(): call(int FieldInitializerJoinPoints.q()) { FieldInitializerJoinPoints.qMcount++; }
}

