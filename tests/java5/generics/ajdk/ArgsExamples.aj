import java.util.*;

class C {
    
    public void foo(List<String> listOfStrings) {}
        
    public void bar(List<Double> listOfDoubles) {}
        
    public void goo(List<? extends Number> listOfSomeNumberType) {}
  
}

aspect A {

    before(List<Double> listOfDoubles) : execution(* C.*(..)) && args(listOfDoubles) {
       for (Double d : listOfDoubles) {
          // do something
       }
    }           
    
    @org.aspectj.lang.annotation.SuppressAjWarnings
    before(List<Double> listOfDoubles) : execution(* C.*(..)) && args(listOfDoubles) {
        for (Double d : listOfDoubles) {
           // do something
        }
     }         
    
    @org.aspectj.lang.annotation.SuppressAjWarnings("uncheckedArgument")
    before(List<Double> listOfDoubles) : execution(* C.*(..)) && args(listOfDoubles) {
        for (Double d : listOfDoubles) {
           // do something
        }
     }         
    
    before(List<Double> listOfDoubles) : execution(* C.*(List<Double>)) && args(listOfDoubles) {
        for (Double d : listOfDoubles) {
           // do something
        }
     }      
    
}

public aspect ArgsExamples {
	
	before() : args(List) && execution(* *(..)) {
		System.out.println("args(List)");
	}
	
	before() : args(List<String>) && execution(* *(..)) {
		System.out.println("args List of String");
	}
	
	before() : args(List<Double>) && execution(* *(..)) {
		System.out.println("args List of Double");
	}
	
	public static void main(String[] args) {
		C c = new C();
		c.foo(new ArrayList<String>());
		c.bar(new ArrayList<Double>());
		c.goo(new ArrayList<Float>());
	}
}