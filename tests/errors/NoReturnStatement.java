import org.aspectj.testing.Tester;

// PR#280 return statement not present in around with returns

public aspect NoReturnStatement {
    public static void main(String[] args) { test(); }
    
    public static void test() {
	    Tester.check(true, "passed");
    }
    
    public int m() { return 1; }
    
    int around(C t): target(t) && call(int m()) {
	    int x = proceed(t);
        // uncomment this to make code compile:
        //    return x;
    }
}

class C {
    public int m() { return 1; }
}
