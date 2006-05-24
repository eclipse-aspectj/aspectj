// Exploring synchronization

public aspect Parsing1 {

	before(): lock() { }
	
	public static void main(String[] args) {
		staticM();
	}
	
	public static void staticM() {
//		synchronized (String.class) {}
	}
}