public class BadExtension { }

abstract class Super {
    public final void finalPublic() {}

    public void justPublic() {}
    public int intPublic() {}

    public abstract void abstractWithBody() {} //ERROR shouldn't have a body

    public abstract void abstractPublic();

    public static final void staticFinalPublic() {}
}

class Sub extends Super { //ERROR must implement abstractPublic
    public void finalPublic() {} //ERROR can't override final
    void justPublic() {} //ERROR can't override with weaker access
    public void intPublic() {} //ERROR can't change the return type

    public static void staticFinalPublic() {} //ERROR can't even override static finals
}



interface I1 {
    void m();
}

interface I2 {
    int m();
}

class C12 implements I1, I2 {
    public void m() {}  //ERROR incompatible return types with I2.m()
}

interface I12 extends I1, I2 {} //ERROR I1.m() and I2.m() are not compatible
