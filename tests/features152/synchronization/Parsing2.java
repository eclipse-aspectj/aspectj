// Exploring synchronization

public aspect Parsing2 {

	before(): unlock() { }
	
	public static void main(String[] args) {
		staticM();
	}
	
	public static void staticM() {
//		synchronized (String.class) {}
	}
}