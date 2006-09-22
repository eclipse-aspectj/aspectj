import java.sql.SQLException;

public aspect Softener2 {

	// don't expect this to soften the exception thrown
	declare soft: Throwable: execution(* SoftenInner.foo());
		
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
