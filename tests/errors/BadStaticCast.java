// Incorrect static casts to primitively foldable arguments should not
// crash the compiler.
public class BadStaticCast {
    public static int foo() {
	return 3 + (int)true;
    }
}

