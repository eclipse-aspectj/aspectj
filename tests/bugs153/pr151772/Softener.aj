import java.sql.SQLException;

public aspect Softener {

	// expect this to soften the exception thrown
	declare soft: Throwable : execution(* *.run());
}

class SoftenInner {
    public static void main(String args[]) {
        new SoftenInner().foo();
    }

    public void foo()  {
        new Runnable() {
            public void run() {
					throw new SQLException("test");
            }
        }.run();
    }

}
