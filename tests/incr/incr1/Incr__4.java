import org.aspectj.testing.Tester;

public class Incr {
	public static void main(String[] args) {
		Tester.checkCurrentRun(4);
		Tester.checkWasRecompiled(4,"Incr");
	}
}
