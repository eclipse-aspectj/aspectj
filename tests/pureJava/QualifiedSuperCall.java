import org.aspectj.testing.Tester;

class PackageSuper {
    String field = "PackageSuper";

    void m() {
        Tester.event("PackageSuper.m()");
    }
}

public class QualifiedSuperCall extends PackageSuper{
    String field = "Package";

    class InnerSuper {
        String field = "InnerSuper";

        void m() {
            Tester.event("InnerSuper.m()");
        }
    }

    class Inner {
        String field = "Inner";
        
        Inner() {
            QualifiedSuperCall.super.m();
            Tester.checkAndClearEvents(new String[] { "PackageSuper.m()" } );
        }


        void m() {
            Tester.event("Inner.m()");
            Tester.event("QualifiedSuperCall.super.field = " + 
                         QualifiedSuperCall.super.field);
            QualifiedSuperCall.super.m();
        }

    }

    public static void main(String[] args) {
        new QualifiedSuperCall().new Inner().m();
        Tester.checkEvents(new String[] { "Inner.m()", "PackageSuper.m()",
                                              "QualifiedSuperCall.super.field = PackageSuper"});
    }

    void m() {
        Tester.event("Package.m()");
    }
}

