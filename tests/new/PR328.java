import java.lang.reflect.*;

public class PR328 {
    public static void main(String[] args) {
        new PR328().realMain(args);
    }
    public void realMain(String[] args) {
        try {
            Class modest = Class.forName("Modest");
            int modifiers = modest.getModifiers();
            boolean isPublic = (modifiers & Modifier.PUBLIC) != 0;
            org.aspectj.testing.Tester.check(!isPublic, "Modest shouldn't be public");
        } catch (Throwable t) {
            org.aspectj.testing.Tester.check(false, "Thrown: " + t);
        }
    }
}
class Modest {}
