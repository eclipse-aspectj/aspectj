import java.util.*;

public aspect SimpleParameterizedTypeExamples {
	
	declare warning : get(List Foo.myStrings) : "get myStrings 1";
	declare warning : get(List<String> Foo.myStrings) : "get myStrings 2";
	declare warning : get(List<Number> *) : "get myStrings 3 - no match";

	declare warning : get(List Foo.myFloats) : "get myFloats 1";
	declare warning : get(List<Float> *) : "get myFloats 2";
	declare warning : get(List<Number+> *) : "get myFloats 3";
	declare warning : get(List<Double> *) : "get myFloats 4 - no match";
	
	declare warning : execution(List get*(..)) : "getter 1";
	declare warning : execution(List<*> get*(..)) : "getter 2";
	declare warning : execution(List<String> get*(..)) : "getter 3";
	declare warning : execution(List<Number+> get*(..)) : "getter 4";

	declare warning : call(* addStrings(List)) : "call 1";
	declare warning : call(* addStrings(List<String>)) : "call 2";
	declare warning : call(* addStrings(List<Number>)) : "call 3 - no match";
	
	void bar() {
		Foo f = new Foo();
		f.addStrings(null);
	}
}

class Foo {
    
    List<String> myStrings;
    List<Float>  myFloats;
        
    public List<String> getStrings() { return myStrings; }
    public List<Float> getFloats() { return myFloats; }
        
    public void addStrings(List<String> evenMoreStrings) {
       myStrings.addAll(evenMoreStrings);   
    }
        
}