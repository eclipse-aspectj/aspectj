// join points in static/dynamic initializers aren't showing up.
import org.aspectj.testing.Tester;

public class NonexistentInitializers {


    public static void main(String[] args) {
	new NonexistentInitializers();
	org.aspectj.testing.Tester.checkEqual
            (A.i, 4, "Not finding some join points in initializers");
        org.aspectj.testing.Tester.checkEqual(A.foo, 2, "foo");
        org.aspectj.testing.Tester.checkEqual(A.get, 2, "get");
    }

    static void foo() {}
    static void bar(Object o) {}
    {
	bar(System.in);
	NonexistentInitializers.foo();
    }
    static {
	bar(System.in);
	NonexistentInitializers.foo();
    }
    

}

aspect A {
    static int i = 0;
    static int foo = 0;
    static int get = 0;

    before(): call(void NonexistentInitializers.foo())  {
	i++;
        foo++;
    }
    before(): get(* System.in)  {
	i++;
        get++;
    }
}

