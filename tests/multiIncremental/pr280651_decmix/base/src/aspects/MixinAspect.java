package aspects;

import org.aspectj.lang.annotation.*;

@Aspect
public class MixinAspect {
    @DeclareMixin(value = "test.Foo")
    public static Runnable foo(Object target) {
        return new DebugDefault();
    }

    public static class DebugDefault implements Runnable {
        public void run() {
            System.out.println("Hi there from MixinAspect");
        }
    }
}

