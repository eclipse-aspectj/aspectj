import org.aspectj.testing.Tester; 
public class D {
    public void d() { a("D.d()"); }
    public void d(C c) { a("D.d(C)"); }
    public void d(C c, C.I ci) { a("D.d(C,C$I)"); }
    public void d(C c, C.I.J cij) { a("D.d(C,C$I$J)"); }
    public void d(C c, C.I.J.K cijk) { a("D.d(C,C$I$J$K)"); }
    public void d(C c, C.I ci, C.I.J cij) { a("D.d(C,C$I,C$I$J)"); }
    public void d(C c, C.I ci, C.I.J.K cijk) { a("D.d(C,C$I,C$I$J$K)"); }
    public void d(C c, C.I.J cij, C.I.J.K cijk) { a("D.d(C,C$I$J,C$I$J$K)"); }
    public void d(C c, C.I ci, C.I.J cij, C.I.J.K cijk) { a("D.d(C,C$I,C$I$J,C$I$J$K)"); }
    public void d(C.I ci) { a("D.d(C$I)"); }
    public void d(C.I.J cij) { a("D.d(C$I$J)"); }
    public void d(C.I.J.K cijk) { a("D.d(C$I$J$K)"); }
    public void d(C.I ci, C.I.J cij) { a("D.d(C$I,C$I$J)"); }
    public void d(C.I ci, C.I.J.K cijk) { a("D.d(C$I,C$I$J$K)"); }
    public void d(C.I.J cij, C.I.J.K cijk) { a("D.d(C$I$J,C$I$J$K)"); }
    public void d(C.I ci, C.I.J cij, C.I.J.K cijk) { a("D.d(C$I,C$I$J,C$I$J$K)"); }

    public static void a(String msg) { Tester.event(msg); }
}
