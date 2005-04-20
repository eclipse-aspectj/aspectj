import java.util.*;

public aspect ITDReturningParameterizedType {
	
	private List<String> myStrings = new ArrayList<String>();
	
	private List<String> C.strings = new ArrayList<String>();
	
	public List<String> C.getStrings() {
		return strings;
	}
	
	
	public static void main(String[] args) {
		C c = new C();
		List<String> ss = c.getStrings();
	}
}


class C {
	
}