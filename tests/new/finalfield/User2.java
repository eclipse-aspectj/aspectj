import org.aspectj.testing.Tester;

public class User2 {
	public static void main(String[] args) {
		switch(Receiver.constant) {
			case 2:
				return;
		}
		Tester.checkFailed("shouldn't get here");
	}
}