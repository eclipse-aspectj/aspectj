// This test verifies that join points exist in the execution of field initializers. 

import org.aspectj.testing.Tester;

public class FieldInitializerJoinPoints {
    
    static int jcount = 0;
    static int kcount = 0;
    static int lcount = 0;
    static int mcount = 0;

    static int ncount = 0;
    static int ocount = 0;
    static int pcount = 0;
    static int qcount = 0;

    static final int j = 99; // not even a set join point here. 
    static int k = 98;       // a set join point
    final int l = 97;        // not a join point (NEW)
    int m = 96;              // yet another set join point  

    static int n() { return 95; }
    static int o() { return 94; }
    static int p() { return 93; }
    static int q() { return 92; }

    static final int n = n(); // a call join point
    static int o = o();       // a call join point
    final int p = p();        // a call join point
    int q = q();              // a call join point

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
    }

}


aspect A {
    before(): set(int FieldInitializerJoinPoints.j) { FieldInitializerJoinPoints.jcount++; }
    before(): set(int FieldInitializerJoinPoints.k) { FieldInitializerJoinPoints.kcount++; }
    before(): set(int FieldInitializerJoinPoints.l) { FieldInitializerJoinPoints.lcount++; }
    before(): set(int FieldInitializerJoinPoints.m) { FieldInitializerJoinPoints.mcount++; }

    before(): call(int FieldInitializerJoinPoints.n()) { FieldInitializerJoinPoints.ncount++; }
    before(): call(int FieldInitializerJoinPoints.o()) { FieldInitializerJoinPoints.ocount++; }
    before(): call(int FieldInitializerJoinPoints.p()) { FieldInitializerJoinPoints.pcount++; }
    before(): call(int FieldInitializerJoinPoints.q()) { FieldInitializerJoinPoints.qcount++; }
}

