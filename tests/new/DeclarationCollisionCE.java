
/** @testcase expect CE for declaration collision between subaspects instead of domination order */
public class DeclarationCollisionCE {
    public static void main (String[] args) {
        C s = new C();
        System.err.println("title: " + s.title());
    } 
}

class C {}

// bug: dominates clause prevents collision error
abstract aspect AA { declare precedence: AA, B; 
    // same result if this line is uncommented
    //public String C.title() { return "[AA] C.title()"; }
}

aspect A extends AA { // implicitly dominates AA
    // dominates AA's declaration, overriding Super.title
    public String C.title() {           // CE 20 collision with B declaration
        return "[A] C.title()" ; 
    }
}

aspect B extends AA { // explicitly dominated by AA ?? --> and hence by A??
    // B fails to dominate AA's declaration, overriding Super.title
    public String C.title() {           // CE 27 collision with A declaration
        return "[B] C.title()" ; 
    }
}

