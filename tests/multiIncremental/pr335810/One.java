import java.util.List;

public class One {

	@SuppressWarnings("rawtypes")
	public void m(List l) {
		
	}
	
	@SuppressWarnings("unchecked")
	public void m2(List<?> input) {
		List<String> ls = (List<String>)input;
		System.out.println(ls);
	}
}

