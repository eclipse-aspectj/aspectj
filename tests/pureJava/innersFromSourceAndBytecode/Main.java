import org.aspectj.testing.Tester; 
/*
 * When reading bytecode and source methods with
 * inner-class parameter types aren't being found.
 */
public class Main {
    public static void main(String[] args) {
        new Main().realMain(args);
    }
    public void realMain(String[] args) {

        m("D.d()");
        m("D.d(C)");
        m("D.d(C,C$I)");
        m("D.d(C,C$I$J)");
        m("D.d(C,C$I$J$K)");
        m("D.d(C,C$I,C$I$J)");
        m("D.d(C,C$I,C$I$J$K)");
        m("D.d(C,C$I$J,C$I$J$K)");
        m("D.d(C,C$I,C$I$J,C$I$J$K)");
        m("D.d(C$I)");
        m("D.d(C$I$J)");
        m("D.d(C$I$J$K)");
        m("D.d(C$I,C$I$J)");
        m("D.d(C$I,C$I$J$K)");
        m("D.d(C$I$J,C$I$J$K)");
        m("D.d(C$I,C$I$J,C$I$J$K)");

        m("C.c()");
        m("C.c(C)");
        m("C.c(C,I)");
        m("C.c(C,I$J)");
        m("C.c(C,I$J$K)");
        m("C.c(C,I,I$J)");
        m("C.c(C,I,I$J$K)");
        m("C.c(C,I$J,I$J$K)");
        m("C.c(C,I,I$J,I$J$K)");
        m("C.c(I)");
        m("C.c(I$J)");
        m("C.c(I$J$K)");
        m("C.c(I,I$J)");
        m("C.c(I,I$J$K)");
        m("C.c(I$J,I$J$K)");
        m("C.c(I,I$J,I$J$K)");

        m("C$I.i()");
        m("C$I.i(C)");
        m("C$I.i(C,I)");
        m("C$I.i(C,J)");
        m("C$I.i(C,J$K)");
        m("C$I.i(C,I,J)");
        m("C$I.i(C,I,J$K)");
        m("C$I.i(C,J,J$K)");
        m("C$I.i(C,I,J,J$K)");
        m("C$I.i(I)");
        m("C$I.i(J)");
        m("C$I.i(J$K)");
        m("C$I.i(I,J)");
        m("C$I.i(I,J$K)");
        m("C$I.i(J,J$K)");
        m("C$I.i(I,J,J$K)");

        m("C$I$J.j()");
        m("C$I$J.j(C)");
        m("C$I$J.j(C,I)");
        m("C$I$J.j(C,J)");
        m("C$I$J.j(C,K)");
        m("C$I$J.j(C,I,J)");
        m("C$I$J.j(C,I,K)");
        m("C$I$J.j(C,J,K)");
        m("C$I$J.j(C,I,J,K)");
        m("C$I$J.j(I)");
        m("C$I$J.j(J)");
        m("C$I$J.j(K)");
        m("C$I$J.j(I,J)");
        m("C$I$J.j(I,K)");
        m("C$I$J.j(J,K)");
        m("C$I$J.j(I,J,K)");

        m("C$I$J$K.k()");
        m("C$I$J$K.k(C)");
        m("C$I$J$K.k(C,I)");
        m("C$I$J$K.k(C,J)");
        m("C$I$J$K.k(C,K)");
        m("C$I$J$K.k(C,I,J)");
        m("C$I$J$K.k(C,I,K)");
        m("C$I$J$K.k(C,J,K)");
        m("C$I$J$K.k(C,I,J,K)");
        m("C$I$J$K.k(I)");
        m("C$I$J$K.k(J)");
        m("C$I$J$K.k(K)");
        m("C$I$J$K.k(I,J)");
        m("C$I$J$K.k(I,K)");
        m("C$I$J$K.k(J,K)");
        m("C$I$J$K.k(I,J,K)");

        D       d    = new D();
        C       c    = new C();
        C.I     ci   = new C.I();
        C.I.J   cij  = new C.I.J();
        C.I.J.K cijk = new C.I.J.K();

        d.d();
        d.d(c);
        d.d(c,ci);
        d.d(c,cij);
        d.d(c,cijk);
        d.d(c,ci,cij);
        d.d(c,ci,cijk);
        d.d(c,cij,cijk);
        d.d(c,ci,cij,cijk);
        d.d(ci);
        d.d(cij);
        d.d(cijk);
        d.d(ci,cij);
        d.d(ci,cijk);
        d.d(cij,cijk);
        d.d(ci,cij,cijk);

        c.c();
        c.c(c);
        c.c(c,ci);
        c.c(c,cij);
        c.c(c,cijk);
        c.c(c,ci,cij);
        c.c(c,ci,cijk);
        c.c(c,cij,cijk);
        c.c(c,ci,cij,cijk);
        c.c(ci);
        c.c(cij);
        c.c(cijk);
        c.c(ci,cij);
        c.c(ci,cijk);
        c.c(cij,cijk);
        c.c(ci,cij,cijk);

        ci.i();
        ci.i(c);
        ci.i(c,ci);
        ci.i(c,cij);
        ci.i(c,cijk);
        ci.i(c,ci,cij);
        ci.i(c,ci,cijk);
        ci.i(c,cij,cijk);
        ci.i(c,ci,cij,cijk);
        ci.i(ci);
        ci.i(cij);
        ci.i(cijk);
        ci.i(ci,cij);
        ci.i(ci,cijk);
        ci.i(cij,cijk);
        ci.i(ci,cij,cijk);

        cij.j();
        cij.j(c);
        cij.j(c,ci);
        cij.j(c,cij);
        cij.j(c,cijk);
        cij.j(c,ci,cij);
        cij.j(c,ci,cijk);
        cij.j(c,cij,cijk);
        cij.j(c,ci,cij,cijk);
        cij.j(ci);
        cij.j(cij);
        cij.j(cijk);
        cij.j(ci,cij);
        cij.j(ci,cijk);
        cij.j(cij,cijk);
        cij.j(ci,cij,cijk);

        cijk.k();
        cijk.k(c);
        cijk.k(c,ci);
        cijk.k(c,cij);
        cijk.k(c,cijk);
        cijk.k(c,ci,cij);
        cijk.k(c,ci,cijk);
        cijk.k(c,cij,cijk);
        cijk.k(c,ci,cij,cijk);
        cijk.k(ci);
        cijk.k(cij);
        cijk.k(cijk);
        cijk.k(ci,cij);
        cijk.k(ci,cijk);
        cijk.k(cij,cijk);
        cijk.k(ci,cij,cijk);

        Tester.checkAllEvents();
    }

    public static void m(String msg) { Tester.expectEvent(msg); }
}
