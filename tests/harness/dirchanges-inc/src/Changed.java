
import org.aspectj.testing.Tester;

public class Changed {
    public static void main(String[] args) {
        Unchanged.main(args);
        String sargs = java.util.Arrays.asList(args).toString();
        if (!"[first]".equals(sargs)) {
            throw new Error("expected args [first] but got " + sargs);
        }
	}
}