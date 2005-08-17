import java.util.List;

public aspect SignatureWildcards {
	
	declare warning : execution(* C.*(List)) : "any list";
	declare warning : execution(* C.*(List<? extends Number>)) : "only foo";
	declare warning : execution(* C.*(List<?>)) : "some list";
	declare warning : execution(* C.*(List<? extends Object+>)) : "any list with upper bound";
}

class C {

    public void foo(List<? extends Number> listOfSomeNumberType) {}
    
    public void bar(List<?> listOfSomeType) {}
    
    public void goo(List<Double> listOfDoubles) {}	

  }