package trace;

public abstract aspect MegaTrace {
	abstract pointcut where();
	
	Object around(): where() {
		System.out.println("around enter: " + thisJoinPointStaticPart);
		Object ret = proceed();
		System.out.println("around exit: " + thisJoinPointStaticPart);
		return ret;
	}
	
	before(): where() {
		System.out.println("enter: " + thisJoinPointStaticPart);
	}
	
	after(): where() {
		System.out.println("exit: " + thisJoinPointStaticPart);
	}
	

	declare warning: where() && execution(void main(..)):
		"tracing execution of main";
		
	public interface Marker{}
	private String Marker.name = "foo-bar";
	
	public String sayHi() { return "hi"; }
	
	public static String getName(Marker m) {
		return m.name;
	}
}