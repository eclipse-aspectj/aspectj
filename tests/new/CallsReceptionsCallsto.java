// lifted directly from Mark's email

import org.aspectj.testing.Tester;

public class CallsReceptionsCallsto {

    static final String desiredString = 
	"(calls) (callsto) (receptions) G.foo(A) G.foo(D) (calls) (callsto) (receptions) G.foo(A) G.foo(D) ";

    static StringBuffer buffer;

    public static void main(String[] args) {
	buffer = new StringBuffer();
	new C().go();
	String foundString = buffer.toString();
        Tester.checkEqual(foundString, desiredString);
    }



}

class C {
    A a = new A();
    D d = new D();
    G g = new G();

    void go(){
	g.foo(a);
	g.foo(d);
	bar(g);
	bash(g);
    }

    void bar(I i){
	i.foo(a);
    }

    void bash(J j){
	j.foo(d);
    }
}

aspect Q {

    pointcut pc2(): /*calls*/  within(C) && call(void I.foo(*));
    pointcut pc1(): /*receptions*/ call(void I.foo(*));
    pointcut pc3(): /*callsto*/ call(* I.foo(*));
 
    before (): pc2() {
	CallsReceptionsCallsto.buffer.append("(calls) ");
    }

    before(): pc3(){
	CallsReceptionsCallsto.buffer.append("(callsto) ");
    }

    before(): pc1() {
	CallsReceptionsCallsto.buffer.append("(receptions) ");
    }
}


class A {}


class D {}


interface I {
    void foo(A a);
}

interface J {
    void foo(D d);
}

class G implements I, J {
    public void foo(A a){
	CallsReceptionsCallsto.buffer.append("G.foo(A) ");
    }
    public void foo(D d){
	CallsReceptionsCallsto.buffer.append("G.foo(D) ");
    }
}
