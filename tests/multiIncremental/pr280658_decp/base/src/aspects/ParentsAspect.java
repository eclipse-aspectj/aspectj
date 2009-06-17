package aspects;
import org.aspectj.lang.annotation.*;
@Aspect
public class ParentsAspect {

    @DeclareParents(value = "test.Foo", defaultImpl = DebugDefault.class)
    public Runnable runnable;

    public static class DebugDefault implements Runnable {
        public void run() {
            System.out.println("hi there from ParentsAspect");
        }
    }
}


