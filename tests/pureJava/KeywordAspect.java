import org.aspectj.testing.Tester;
public class KeywordAspect {
    public static void main(String[] args) {
        new KeywordAspect().realMain(args);
    }

    String pointcut = "hi";

    public void realMain(String[] args) {
        int aspect = 0;
        aspect += 10;

        Tester.checkEqual(aspect, 10);
        Tester.checkEqual(pointcut, "hi-bye");
    }
    
    public KeywordAspect() {
	pointcut += "-bye";
    }
}
