
import org.aspectj.testing.*;

// todo: package-qualified calls
/** PR651 PUREJAVA qualified super method calls */
public class QualifiedSuperCall {
    public static void main (String[] args) {
        Super s;
        s = new Super();
        Tester.check(null != s, "new Super()");
        s = new Sub();
        Tester.check(null != s, "new Sub()");
        s = new UnqualifiedSub();
        Tester.check(null != s, "new UnqualifiedSub()");
        s = new SubSub();
        Tester.check(null != s, "new SubSub()");
        s = new UnqualifiedSubSub();
        Tester.check(null != s, "new UnqualifiedSubSub()");
        s = new Mid();
        Tester.check(null != s, "new Mid()");
        s = new SubMid();
        Tester.check(null != s, "new SubMid()");
        s = new UnqualifiedSubMid();
        Tester.check(null != s, "new UnqualifiedSubMid()");
    } 
    static {
        Tester.m("Super.duper=Sub()");
        Tester.m("Super.duper=UnqualifiedSub()");
        Tester.m("Super.duper=SubSub()");
        Tester.m("Super.duper=UnqualifiedSubSub()");
        Tester.m("Super.duper=SubMid()");
        Tester.m("Mid.duper=SubMid()");
        Tester.m("Mid.duper=UnqualifiedSubMid()");
    }
    
}

class Super {
    Super() {}
    void duper(String caller) {
        Tester.event("Super.duper=" + caller);
    }
}

class Sub extends Super {
    Sub() {
        Super.super();
        Super.super.duper("Sub()");
    }
}
class UnqualifiedSub extends Super {
    UnqualifiedSub() {
        super();
        super.duper("UnqualifiedSub()");
    }
}

class SubSub extends Sub {
    SubSub() {
        Sub.super();
        Sub.super.duper("SubSub()");
    }
}
class UnqualifiedSubSub extends UnqualifiedSub {
    UnqualifiedSubSub() {
        super();
        super.duper("UnqualifiedSubSub()");
    }
}

class Mid extends Super {
    Mid() { }
    void duper(String caller) {
        Tester.event("Mid.duper=" + caller);
    }
}

class SubMid extends Mid {
    SubMid() {
        Mid.super(); // XXX illegal ordering?
        Super.super(); 
        Super.super.duper("SubMid()");
        Mid.super.duper("SubMid()");
    }
}
class UnqualifiedSubMid extends Mid {
    UnqualifiedSubMid() {
        super(); 
        super.duper("UnqualifiedSubMid()");
    }
}


