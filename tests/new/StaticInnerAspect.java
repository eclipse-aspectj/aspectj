import org.aspectj.testing.*;

public class StaticInnerAspect {
    public static void main(String[] args) {
        Tester.check(true, "compiled");
    }

}

aspect Aspect {
    static class InnerClass {        
        static aspect InnerAspect {
        }
    }
}
