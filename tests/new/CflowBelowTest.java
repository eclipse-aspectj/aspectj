import org.aspectj.testing.Tester;

import java.util.*;

/**
 * Inspired by aspect sandbox and submitted by Chris Dutchyn
 */
public class CflowBelowTest {
    static final String[] expectedSteps = new String[] {
        "Num.fact(4) within Num.fact(5) within another Num.fact(6)",
        "Num.fact(3) within Num.fact(4) within another Num.fact(5)",
        "Num.fact(2) within Num.fact(3) within another Num.fact(4)",
        "Num.fact(1) within Num.fact(2) within another Num.fact(3)",
    };

    static List steps = new ArrayList();

    static public void main( String[] args ) {
        Tester.checkEqual(new Num().fact(6), 720);

        Tester.checkEqual(steps.toArray(), expectedSteps, "steps");
    }
}


class Num {
    int fact(int x) {
	if (x == 1)
	    return 1;
	return x * fact(x - 1);
    }
}

// check that cflows of nested calls obtain correct parameters
aspect CflowBelow01 {
    before (int x1, int x2, int x3) :
	call(int Num.fact(int)) && args(x1)
	&& cflowbelow(call(int Num.fact(int)) && args(x2)
		      && cflowbelow(call(int Num.fact(int)) && args(x3))) {
        CflowBelowTest.steps.add("Num.fact(" + x1 +
			   ") within Num.fact(" + x2 +
			   ") within another Num.fact(" + x3 + ")");
    }
}
