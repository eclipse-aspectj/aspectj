import java.io.Serializable;
@InjectName
@MarkMyMethods
public class Main {

	public String name;
	
	public Main() {
		name = "jack";
	}
	
	public String testMethod() {
		return "Test";
	}
	
	protected String testMethodProtected() {
		return "Blah!";
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Main m = new Main();
		Class[] cls = Main.class.getInterfaces();		
		for(Class cl:cls) {
			System.out.println("Interface : " + cl.getCanonicalName());
		}
	}

}
