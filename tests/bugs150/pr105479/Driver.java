public class Driver {
	
	public static void main(String[] args) {
		ReturnTypeTester rtt = new ReturnTypeTester();
		rtt.hashCode();
		System.out.println(rtt.getId());
		if (rtt.hashCode() != "id".hashCode()) throw new RuntimeException("dispatch failure");
	}
	
}