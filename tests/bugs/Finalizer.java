// for Bug#:  30026
import org.aspectj.testing.Tester;


public class Finalizer {
    public static void main(String args[]) {
        Finalizer np = new Finalizer();
        np = null;
    }

    public void finalize() throws Throwable {
    }
}

aspect FinalizeContract {
    pointcut finalizeCall(Object o):
        this(Object+) &&
        this(o) &&
        execution(void finalize());

    void around(Object o) throws Throwable: finalizeCall(o) {
        o.finalize();               // error
        //((Finalizer) o).finalize();   // ok
        proceed(o);
    }
}
