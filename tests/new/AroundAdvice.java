import org.aspectj.testing.Tester;

public aspect AroundAdvice {
    public static void main(String[] args) { test(); }
    
    public static void test() {
	Tester.checkEqual(new Foo().foo(5), 1+2+3+4+5, "sum");
	Tester.checkEqual(new Foo().bar(), "overridden", "bar()");
    }
    
    pointcut fooCut(int n): target(Foo) && call(int foo(int)) && args(n);
    
    int around(int n): fooCut(n) {
        int N = n;
        int sum = 0;
        for(int i=0; i<N; i++) {
            n = i;
            int ret = proceed(n);
            sum += ret;
        }
        return sum;
    }
    
    String around(): within(AroundAdvice) && call(String bar()) {
        return "overridden";
    }
}

class Foo {
    public int foo(int x) {
	//System.out.println("foo("+x+")");
	return x+1;
    }
    public String bar() {
	return "bar()";
    }
}
