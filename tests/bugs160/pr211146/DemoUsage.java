public class DemoUsage {
	public static void main(String[] args) {
		StringClass sc = new StringClass();
		sc.getStrings().add("AspectJ");
		for(String s : sc.getStrings()) {
			System.out.println(s);
		}
	}
}