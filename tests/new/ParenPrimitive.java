import org.aspectj.testing.Tester;

public class ParenPrimitive {
    public static void main(String[] args) {
	Tester.checkEqual(typenameFor(null), "int");
    }

    private static String typenameFor(String type) {
	return("int");
    }
}
