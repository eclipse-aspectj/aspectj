import org.aspectj.testing.Tester;

public class Incr {
	public static void main(String[] args) {
		Tester.checkCurrentRun(2,3);
		Tester.checkWasRecompiled(2,"Incr");
		Tester.checkWasNotRecompiled(3,"Incr");
	}
}
