import org.aspectj.testing.Tester;

public class DeclareError {
    public static void main(String[] args) {
        new C().bad();
    }
}

class C {
    public void m() {
        new C().bad();
    }

    public void bad() { }
}

class D {
    public void m() {
        new C().bad();
    }
}


aspect A {
    declare error: call(void C.bad()) && !within(C):
        "can only call bad from C";
}
    
