import org.aspectj.testing.Tester;

public class DivOpMustHappen {
    static int i = 0;
    static int j = 1;

    public static void main(String[] args) {
	boolean threw = false;
	try {
	    switch(j / i) {}
	} catch (Exception e) {
	    threw = true;
	}
	Tester.check(threw, "didn't throw divbyzero exception");
    }
}

