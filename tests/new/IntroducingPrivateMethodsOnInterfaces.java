import org.aspectj.testing.Tester;
import org.aspectj.testing.Tester; 
public class IntroducingPrivateMethodsOnInterfaces {
    public static void main(String[] args) {
        AA.go();
    }
    static {
        Tester.expectEvent("private_private");
        Tester.expectEvent("private_package");
        Tester.expectEvent("private_public");
        Tester.expectEvent("package_private");
        Tester.expectEvent("package_package");
        Tester.expectEvent("package_public");
        Tester.expectEvent("public_private");
        Tester.expectEvent("public_package");
        Tester.expectEvent("public_public");        
    }
}

interface I {}
class C {

//      private     void _private(String s)   { Tester.event("private_"+s);   }
//      /*package*/ void _package(String s)   { Tester.event("package_"+s);   }
//      public      void _public(String s)    { Tester.event("public_"+s);    }
    
//      private void private_private() { _private("private"); }
//      private void private_package() { _package("private"); }
//      private void private_public()  { _public("private");  }

//      /*package*/ void package_private() { _private("package"); }
//      /*package*/ void package_package() { _package("package"); }
//      /*package*/ void package_public()  { _public("package");  }

//      public void public_private() { _private("public"); }
//      public void public_package() { _package("public"); }
//      public void public_public()  { _public("public");  }    

}

aspect AA {
    
    public static void go() {
        I c = new C();
        c.private_private();
        c.private_package();
        c.private_public();
        
        c.package_private();
        c.package_package();
        c.package_public();

        c.public_private();
        c.public_package();
        c.public_public();
    }
    
    private     void I._private(String s)   { Tester.event("private_"+s);   }
    /*package*/ void I._package(String s)   { Tester.event("package_"+s);   }
    public      void I._public(String s)    { Tester.event("public_"+s);    }
    
    private void I.private_private() { _private("private"); }
    private void I.private_package() { _package("private"); }
    private void I.private_public()  { _public("private");  }

    /*package*/ void I.package_private() { _private("package"); }
    /*package*/ void I.package_package() { _package("package"); }
    /*package*/ void I.package_public()  { _public("package");  }

    public void I.public_private() { _private("public"); }
    public void I.public_package() { _package("public"); }
    public void I.public_public()  { _public("public");  }
}

aspect A {
    declare parents: C implements I;
}
