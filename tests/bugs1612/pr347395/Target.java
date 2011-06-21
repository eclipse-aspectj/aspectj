package xxx.util;

@TaskModification
public class Target {

	@TaskModification
	public void m() {

	}

	public static void main(String[] args) {

		new Target().m();
	}

}
