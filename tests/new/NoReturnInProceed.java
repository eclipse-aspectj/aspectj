
class BaseApp {
	int i;
    int get()  { return i; }
}

/** @testStatement If there is no return in around returning a result, javac catches error */
public class NoReturnInProceed {
    public static void main(String[] args) {
        BaseApp target = new BaseApp();
		target.get();
    }
}

aspect Aspect {
    int around() : get(int BaseApp.i) {
		proceed();
		// return proceed();
	}
}
