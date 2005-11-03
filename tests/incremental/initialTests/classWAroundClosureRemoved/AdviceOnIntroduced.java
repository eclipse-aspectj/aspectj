import org.aspectj.testing.Tester;

public aspect AdviceOnIntroduced {
    public static void main(String[] args) { test(); }

    public static void test() {
	Tester.checkEqual(new Foo(10).foo(5), 6, "foo");
    }
    
    int Foo.foo(int n) { return n; }
    Foo.new(int w) { this(); }
        
    int around(int n):
        within(AdviceOnIntroduced) &&
        (args(n) && execution(int foo(int))) {
            int result = proceed(n);
            return result+1;
        }
    
    before(): within(Foo) && execution(new(..)) {
	//System.out.println("before new");
    }
}

class Foo {
}
