import org.aspectj.testing.Tester;
public class KeywordPointcut {
    public static void main(String[] args) {
        new KeywordPointcut().realMain(args);
    }
    public void realMain(String[] args) {
        int pointcut = 0;
	pointcut += 2;
        Tester.checkEqual(pointcut, 2);
    }
    
    public KeywordPointcut() {
    }
}
