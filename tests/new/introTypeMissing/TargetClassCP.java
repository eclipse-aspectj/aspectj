import org.aspectj.testing.Tester;

public class TargetClassCP {
    /** run PUREJAVA variant of the tests */
    public static void main(String[] args) {
        SubClass me = new SubClass();
        Tester.check(me.field, "me.field");
        Tester.check(me.f.valid(), "me.f.valid()");
        Tester.check(me.new inner().run(), "me.new inner().run() ");
        Tester.check(me.result_cast, "me.result_cast");
    }
}

class TargetClass {
    boolean ok = true;
    boolean getboolean() { return (this != null); }
    public class InnerClass {
        public boolean valid() { 
            return (null != this);
        }
    }
}

/** @testcase enclosing class available as this qualifier in inner classes */
class SubClass extends TargetClass {
    public class inner {
        public boolean run() {
            InnerClass j = SubClass.this.new InnerClass(); 
            boolean boolean_4 = SubClass.this.getboolean(); 
            return (boolean_4 && j.valid());
        }
    }

    boolean result_cast = SubClass.this.getboolean(); 
    InnerClass f = SubClass.this.new InnerClass(); 
    boolean field = SubClass.this.ok; 
}

