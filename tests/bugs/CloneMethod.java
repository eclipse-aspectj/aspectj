aspect AspectA {
	protected interface I {
	}

	declare parents : MyString implements I;

	protected Object createCloneFor(I object) {
		if (object instanceof MyString) {
			return new MyString(((MyString) object).toString());
		} else {
			return null;
		}
	}

	public Object I.clone() throws CloneNotSupportedException {
		return super.clone();
//		return null;
	}

	public Object cloneObject(I object) {
		try {
			return object.clone();
		} catch (CloneNotSupportedException ex) {
			return createCloneFor(object);
		}
	}
}

class MyString implements Cloneable {

	protected String text;

	public MyString(String init) {
		text = init;
	}

	public void setText(String newText) {
		text = newText;
	}

	public String toString() {
		return "MyString: " + text;
	}
}

public class CloneMethod {

	public static void main(String[] args) {
		MyString orig1;
		MyString copy1;

		orig1 = new MyString("  This is I 1");
		copy1 = (MyString) AspectA.aspectOf().cloneObject(orig1);
		orig1.setText("  This is I 2");
		copy1.setText("  This is Clone 1");
		System.out.println("... done.");
	}
}
