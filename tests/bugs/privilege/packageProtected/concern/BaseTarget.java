package packageProtected.concern;

public class BaseTarget {

    // ---- same as Base
    int default_int;
    public int public_int;
    protected int protected_int;
    private int private_int;

    void default_method() { }
    public void public_method() { }
    protected void protected_method() { }
    private void private_method() { }
    
    void default_method(Object unused) { }
    public void public_method(Object unused) { }
    protected void protected_method(Object unused) { }
    private void private_method(Object unused) { }

    void default_method(Object unused, Object second) { }
    public void public_method(Object unused, Object second) { }
    protected void protected_method(Object unused, Object second) { }
    private void private_method(Object unused, Object second) { }

    static int default_staticInt;
    public static int public_staticInt;
    protected static int protected_staticInt;
    private static int private_staticInt;

    static void default_staticMethod() { }
    public static void public_staticMethod() { }
    protected static void protected_staticMethod() { }
    private static void private_staticMethod() { }
    
    static void default_staticMethod(Object unused) { }
    public static void public_staticMethod(Object unused) { }
    protected static void protected_staticMethod(Object unused) { }
    private static void private_staticMethod(Object unused) { }

    static void default_staticMethod(Object unused, Object second) { }
    public static void public_staticMethod(Object unused, Object second) { }
    protected static void protected_staticMethod(Object unused, Object second) { }
    private static void private_staticMethod(Object unused, Object second) { }
    // ---- end of same as Base


    public void trigger(BaseTarget me) {
    }
    public static void main(String[] args) {
        BaseTarget user = new BaseTarget();
        user.trigger(new BaseTarget());
    }
}
