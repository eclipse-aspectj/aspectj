package pkgA;

public class Target {
	
	public Listener listenA() {
		return new Listener() {
			public void happened(String event) {
				System.out.println(event);
			}
		};
	}
	
	public static void main(String[] args) {
		Target t = new Target();
		t.listenA().happened("Simple A");
		t.listenB().happened("Inferred B");
	}
	
}
