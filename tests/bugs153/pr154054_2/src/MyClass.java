public class MyClass {

	String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static void main(String[] args) {
		MyClass m = new MyClass();
		m.setName("Fred");
		System.out.println(m.getName());
	}

}
