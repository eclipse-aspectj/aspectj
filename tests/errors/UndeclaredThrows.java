import java.io.*;

public class UndeclaredThrows {

}

class C {
    public void m() throws Exception {
    }

    public void m1() {
        m();
    }

    public void m2() {
        try {
            m1();
        } catch (IOException ioe) { }
    }
}
