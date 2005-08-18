import java.util.*;
import org.aspectj.lang.annotation.SuppressAjWarnings;

public aspect AfterReturningExamples {
	
	after() returning : execution(* C.*(..)) {
		System.out.println(thisJoinPointStaticPart);
	}
	
	 pointcut executionOfAnyMethodReturningAList() : execution(List *(..));
	
	 // matches all three
	 after() returning(List<?> listOfSomeType) : executionOfAnyMethodReturningAList() {
         for (Object element : listOfSomeType) {
            System.out.println("raw " + element);
         }
     }
	
	// matches bar and goo, with unchecked on goo
    after() returning(List<Double> listOfDoubles) : execution(* C.*(..)) {
        for(Double d : listOfDoubles) {
           System.out.println("a1 " + d);
        }   
     }
	
    // matches only bar
    after() returning(List<Double> listOfDoubles) : execution(List<Double> C.*(..)) {
        for(Double d : listOfDoubles) {
           System.out.println("a2 " + d);
        }   
     }
    
	// matches bar and goo, with no warning
    @SuppressAjWarnings
    after() returning(List<Double> listOfDoubles) : execution(* C.*(..)) {
        for(Double d : listOfDoubles) {
           System.out.println("a3 " + d);
        }   
     }
    
    
    public static void main(String[] args) {
		List<Double> ld = new ArrayList<Double>();
		ld.add(5.0d);
		ld.add(10.0d);
		List<String> ls = new ArrayList<String>();
		ls.add("s1");
		ls.add("s2");
		C c = new C();
		c.foo(ls);
		c.bar(ld);
		c.goo(ld);
	}
}



class C {
    
    public List<String> foo(List<String> listOfStrings) { return listOfStrings; }
        
    public List<Double> bar(List<Double> listOfDoubles) { return listOfDoubles; }
        
    public List<? extends Number> goo(List<? extends Number> listOfSomeNumberType) { return listOfSomeNumberType; }
  
}