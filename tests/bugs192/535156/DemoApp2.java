import org.aspectj.lang.annotation.*;

public class DemoApp2 {
	public static void main(String[]argv) {}
    private void recurseInsteadOfWhile() {
        say();
    }

    public void say() { }
}

aspect X {
	void around(): call(public void DemoApp2+.say(..)) {
	}
}
