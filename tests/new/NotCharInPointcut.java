// PR#208 ! modifier in pointcut

public class NotCharInPointcut {
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < methods.length; i++) {
            org.aspectj.testing.Tester.expectEvent(methods[i]);
            C.class.getMethod(methods[i], new Class[]{}).
                invoke(new C(), new Object[]{});
        }
    }
    
    final static String[] methods = {
        "_void",
        "_boolean",
        "_byte",
        "_char",
        "_short",
        "_int",
        "_long",
        "_float",
        "_double",
        "_Object",
    };
}

class C {
    private void s(String s) { org.aspectj.testing.Tester.event(s); }
    public void _void() { s("_void"); }
    public boolean _boolean() { s("_boolean"); return (boolean)false;}
    public byte _byte() { s("_byte"); return (byte)0;}
    public char _char() { s("_char"); return (char)0;}
    public short _short() { s("_short"); return (short)0;}
    public int _int() { s("_int"); return (int)0;}
    public long _long() { s("_long"); return (long)0;}
    public float _float() { s("_float"); return (float)0;}
    public double _double() { s("_double"); return (double)0;}
    public Object _Object() { s("_Object"); return this; }
}

aspect A {

    pointcut pcut1(NotCharInPointcut t):
        this(t) && execution(!* _*());
    
    pointcut pcut2(NotCharInPointcut t):
        this(t) && !this(NotCharInPointcut) && execution(!* _*());

    pointcut pcut3(NotCharInPointcut t):
        pcut1(t) || pcut2(t);

    before(NotCharInPointcut t): pcut1(t) { s("1:"+thisJoinPoint); }
    before(): pcut2(*) { s("2:"+thisJoinPoint); }
    before(NotCharInPointcut t): pcut3(t) { s("3:"+thisJoinPoint); }

    private final static void s(Object s) {
        org.aspectj.testing.Tester.check(false, "should be in "+s);
    }
        
}
