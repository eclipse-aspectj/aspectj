
public class TargetClassCF {
    /** run PUREJAVA variant of the tests */
    public static void main(String[] args) {
        throw new Error("expecting compile failure");
    }
}

class TargetClass {
    boolean getboolean() { return (this != null); }
    public class InnerClass {
        public boolean valid() { 
            return (null != this);
        }
    }
}

/** @testcase superclass n/a as this qualifier in inner classes */
class PureJava extends TargetClass {
    public class inner {
        public void run() {
            InnerClass j = TargetClass.this.new InnerClass(); // s.b. PureJava
            boolean boolean_4 = TargetClass.this.getboolean(); // s.b. PureJava
        }
    }

    boolean result_cast = TargetClass.this.getboolean(); // s.b. PureJava
    InnerClass f = TargetClass.this.new InnerClass(); // s.b. PureJava
}

