import java.util.List;
public aspect MixedParameterizedAndTypeVariables {
	
	declare warning : execution(List G.foo(List)) : "erasure match";
	declare warning : execution(List G.foo(List<String>)) : "mixed match";
	declare warning : execution(* *(List<String>)) : "params only match";
	declare warning : execution(List<Object> G.foo(List<String>)) : "wrong erasure";
		
}

class G<T> {
	
	List<T> foo(List<String> ls) { return null; }
	
}