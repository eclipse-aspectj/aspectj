import org.aspectj.testing.Tester;

public class Incr {
	public static void main(String[] args) {
		Tester.checkCurrentRun(0,1);
		Tester.checkWasRecompiled(0,"Incr");
		Tester.checkWasRecompiled(1,"Incr");
	}
}
