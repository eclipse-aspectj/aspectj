import org.aspectj.testing.Tester;

public class ObjectForInt {
    public static void main(String[] args){
	new Test().go();
	Tester.checkEqual(Test.a, 10, "Test.a");
	Tester.checkEqual(A.beforeA.intValue(), 4, "beforeA"); 
    }
}

class Test {
    public static int a = -1;
    void go(){
	foo(4);
    }
    
    void foo(int a){
	Test.a = a;
    }
}

aspect A {
    public static Integer beforeA = null;
    pointcut fooCut(Object i):  
  	target(Test) && args(i) && call(void f*(*));
    
    before(Object o): fooCut(o){ 
	beforeA = (Integer)o;
    }

    void around(Object o): fooCut(o){
	proceed(new Integer(10));
    }
}
