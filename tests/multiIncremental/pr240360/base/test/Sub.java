package test;

public class Sub extends Base {
	private int numValue;

	private String description;

	public Sub(int id, String value, String description, int numValue) {
		super(id, value);
		this.description = description;
		this.numValue = numValue;
	}
	
	public int getNumValue() {
		return numValue;
	}

	public void setNumValue(int numValue) {
		this.numValue = numValue;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void other() {
//		blah;
	}
}
