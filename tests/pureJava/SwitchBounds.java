import org.aspectj.testing.Tester;

public class SwitchBounds {

    static int minZero(int key) {
	switch (key) {
	case Integer.MIN_VALUE:
	    return Integer.MIN_VALUE;
	case 0:
	    return 0;
	default:
	    return key;
	}
    }

    static int maxZero(int key) {
	switch (key) {
	case Integer.MAX_VALUE:
	    return Integer.MAX_VALUE;
	case 0:
	    return 0;
	default:
	    return key;
	}
    }

    static int minMax(int key) {
	switch (key) {
	case Integer.MIN_VALUE:
	    return Integer.MIN_VALUE;
	case Integer.MAX_VALUE:
	    return Integer.MAX_VALUE;
	default:
	    return key;
	}
    }

    static int fiveMins(int key) {
	switch (key) {
	case Integer.MIN_VALUE:
	    return Integer.MIN_VALUE;
	case (Integer.MIN_VALUE + 1):
	    return (Integer.MIN_VALUE + 1);
	case (Integer.MIN_VALUE + 2):
	    return (Integer.MIN_VALUE + 2);
	case (Integer.MIN_VALUE + 3):
	    return (Integer.MIN_VALUE + 3);
	case (Integer.MIN_VALUE + 4):
	    return (Integer.MIN_VALUE + 4);
	case (Integer.MIN_VALUE + 5):
	    return (Integer.MIN_VALUE + 5);
	default:
	    return key;
	}
    }

    public static void main(String[] args) {
	int MIN = Integer.MIN_VALUE;
	int MAX = Integer.MAX_VALUE;

	Tester.checkEqual(MIN, minZero(MIN));
	Tester.checkEqual(0, minZero(0));
	Tester.checkEqual(37, minZero(37));

	Tester.checkEqual(MAX, maxZero(MAX));
	Tester.checkEqual(0, maxZero(0));
	Tester.checkEqual(37, maxZero(37));

	Tester.checkEqual(MIN, minMax(MIN));
	Tester.checkEqual(MAX, minMax(MAX));
	Tester.checkEqual(37, minMax(37));

	Tester.checkEqual(MIN, fiveMins(MIN));
	Tester.checkEqual(MIN + 1, fiveMins(MIN + 1));
	Tester.checkEqual(MIN + 2, fiveMins(MIN + 2));
	Tester.checkEqual(MIN + 3, fiveMins(MIN + 3));
	Tester.checkEqual(MIN + 4, fiveMins(MIN + 4));
	Tester.checkEqual(MIN + 5, fiveMins(MIN + 5));
	Tester.checkEqual(MIN + 37, fiveMins(MIN + 37));
    }
}
