// join points in field initializers aren't showing up.

import org.aspectj.testing.Tester;

public class NonexistentFieldInitializers {
    public static void main(String[] args) {
	new NonexistentFieldInitializers();
	Tester.checkEqual(A.i, 2, "Not finding some join points in initializers");
    }

    static Object so = System.in;

    Object o = System.in;
}

aspect A {
    static int i;

    before(): get(* System.in)  {
	i++;
    }
}

