import org.aspectj.lang.annotation.*;

public class DemoApp {
	public static void main(String[]argv) {}
    private void recurseInsteadOfWhile() {
        say();
    }

    public void say() { }
}

aspect X { // mixed style here...
    @Around("call(public void DemoApp+.say(..))")
    public void y() {}
}
