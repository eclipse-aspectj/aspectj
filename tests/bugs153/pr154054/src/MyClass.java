public class MyClass {

	int x;

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public static void main(String[] args) {
		MyClass m = new MyClass();
		m.setX(10);
		System.out.println(m.getX());
	}

}
