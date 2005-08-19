public class pr74562 {
	
	private pr74562 before;
	private pr74562 after;
	
	public void test() {
		before.after = before;
		after.before = before;
		before = new pr74562();
		after = new pr74562();
	}
	
}