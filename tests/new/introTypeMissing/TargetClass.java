/*
  @testcase definitions for introduced type test cases
 --- initial variants
 // -------- error "can't emit cast for NotFoundType() to PrimitiveType(int)
 //double i = (double) i2() - i1(); 
 //int i = (int) this.someInt();

 // -------- compiler RuntimeException: "Unsupported emit on NotFoundType" Type.java:460
 //double j = (double) (d2() - d1()); 
 //if ((d2() - d1()) instanceof float) { doTrue(); }
 //if ((d2() - d1()) instanceof int) { doTrue(); }
 //if ((d2() - d1()) instanceof short) { doTrue(); }
 //if ((d2() - d1()) instanceof double) { doTrue(); }
 //if ((i2() - i1()) instanceof int) { doTrue(); }
 //if (!((getBoolean()) instanceof boolean)) { Tester.check(false,"boolean"); }
 //if (!((getChar()) instanceof char)) { Tester.check(false,"char"); }
 //if (!((getByte()) instanceof byte)) { Tester.check(false,"byte"); }
 if (d2 instanceof double) { doTrue(); }

 // ------ expecting error, get compiler RuntimeException
 if (!((doVoid()) instanceof Void)) { Tester.check(false,"void"); }

 // -------- NPE NewInstanceExpr.java:287
 //InnerClass i = this.new InnerClass();
 //InnerClass i = getThis().new InnerClass();

--- generalization of initial variants
 primitive type not found: 
   either the introduced-on type or the method type of it

 --- possibly-relevant variants
     [failingTypeExpressions: instanceof, cast, qualified-new]
   x [introduced|normal|subclass]
   x [static|{default}]
   x [methods|field initializers]
   x [types=[String|Object|Type]|primitiveTypes=[boolean|...]]

 --- actual variants
    [failingTypeExpressions] 
  x [introduced|subclass]
  x [methods|field initializers]
  x [primitiveTypes] 
  except cast expression only for introduced methods - uncertain here

*/
   

class Type {}
public class TargetClass {
    TargetClass getThis() { return this ; }
    boolean getboolean() { return (this != null); }
    byte    getbyte() { return '\1'; }
    char    getchar() { return '\1'; }
    short   getshort() { return 0; }
    int     getint() { return 0; }
    long    getlong() { return 1l; }
    float   getfloat() { return 1f; }
    double  getdouble() { return 1d; }
    String  getstring() { return ""; }

    void    doVoid() { }
    Type    getType() { return null; }

    /** run PUREJAVA variant of the tests */
    public static void main(String[] args) {
        PureJava me = new PureJava();
        me.run();
        if (!me.result_cast) Util.fail("me.result_cast");
        if (!me.result_inner) Util.fail("me.result_inner");
    }
    public class InnerClass {
        public boolean valid() { 
            return (null != this);
        }
    }
}

/** PUREJAVA variant of the tests */
class PureJava extends TargetClass {
    public void run() {
        instanceOf();
        cast();
        newInner();
    }

    public void newInner() {
        InnerClass i = this.new InnerClass();
        if (!i.valid()) Util.fail("this.new InnerClass()");
        InnerClass j = getThis().new InnerClass();
        if (!j.valid()) Util.fail("getThis().new InnerClass()");
        Util.signal("inner");
    }
    
    public void cast() {
        boolean boolean_1 = getboolean();
        boolean boolean_2 = (boolean) getboolean();
        boolean boolean_3 = (boolean) this.getboolean();
        byte byte_1 = getbyte();
        byte byte_2 = (byte) getbyte();
        byte byte_3 = (byte) this.getbyte();
        char char_1 = getchar();
        char char_2 = (char) getchar();
        char char_3 = (char) this.getchar();
        short short_1 = getshort();
        short short_2 = (short) getshort();
        short short_3 = (short) this.getshort();
        int int_1 = getint();
        int int_2 = (int) getint();
        int int_3 = (int) this.getint();
        long long_1 = getlong();
        long long_2 = (long) getlong();
        long long_3 = (long) this.getlong();
        float float_1 = getfloat();
        float float_2 = (float) getfloat();
        float float_3 = (float) this.getfloat();
        double double_1 = getdouble();
        double double_2 = (double) getdouble();
        double double_3 = (double) this.getdouble();
        //X X_1 = getX();
        //X X_2 = (X) getX();
        //X X_3 = (X) this.getX();
        Util.signal("cast");
    }
    public void instanceOf() {
        // -------- RuntimeException: "Unsupported emit on NotFoundType" Type.java:460
        /*
        if (!((getBoolean()) instanceof Boolean)) { Util.fail("boolean"); }
        if (!((getChar()) instanceof char))       { Util.fail("char"); }
        if (!((getByte()) instanceof byte))       { Util.fail("byte"); }
        if (!((getShort()) instanceof short))     { Util.fail("short"); }
        if (!((getInt()) instanceof int))         { Util.fail("int"); }
        if (!((getLong()) instanceof long))       { Util.fail("long"); }
        if (!((getFloat()) instanceof float))     { Util.fail("float"); }
        if (!((getDouble()) instanceof double))   { Util.fail("double"); }
        */
        // ------ todo: expecting error, get RuntimeException
        //if (!((doVoid()) instanceof Void)) { Tester.check(false,"void"); }
        Util.signal("instanceOf");
    }    
    // ---------- field initializer
    interface Result  { public boolean run();}
    boolean result_inner =
        new Result() {
                public boolean run() {
                    TargetClass.InnerClass i = ((TargetClass) PureJava.this).new InnerClass();
                    if (!i.valid()) Util.fail("this.new InnerClass()");
                    TargetClass.InnerClass j = ((TargetClass) getThis()).new InnerClass();
                    if (!j.valid()) Util.fail("getThis().new InnerClass()");
                    Util.signal("innerfield");
                    return i.valid() && j.valid();
                }
            }.run();    
    boolean result_cast =
        new Result() {
                public boolean run() {
                    boolean boolean_1 = getboolean();
                    boolean boolean_2 = (boolean) getboolean();
                    boolean boolean_3 = (boolean) PureJava.this.getboolean();
                    byte byte_1 = getbyte();
                    byte byte_2 = (byte) getbyte();
                    byte byte_3 = (byte) PureJava.this.getbyte();
                    char char_1 = getchar();
                    char char_2 = (char) getchar();
                    char char_3 = (char) PureJava.this.getchar();
                    short short_1 = getshort();
                    short short_2 = (short) getshort();
                    short short_3 = (short) PureJava.this.getshort();
                    int int_1 = getint();
                    int int_2 = (int) getint();
                    int int_3 = (int) PureJava.this.getint();
                    long long_1 = getlong();
                    long long_2 = (long) getlong();
                    long long_3 = (long) PureJava.this.getlong();
                    float float_1 = getfloat();
                    float float_2 = (float) getfloat();
                    float float_3 = (float) PureJava.this.getfloat();
                    double double_1 = getdouble();
                    double double_2 = (double) getdouble();
                    double double_3 = (double) PureJava.this.getdouble();
                    //X X_1 = getX();
                    //X X_2 = (X) getX();
                    //X X_3 = (X) this.getX();
                    Util.signal("castfield");
                    return (boolean_1 && boolean_2 && boolean_3);
                }
            }.run();

}

