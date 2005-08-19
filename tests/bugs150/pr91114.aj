public class pr91114 {
	
	public void bar() {
		long before = 0;
		long after = 0;
		long x = (before - after);  // doesn't fail without the parentheses!
		System.out.println("... " + (before - after) + " ...");
	}
	
}