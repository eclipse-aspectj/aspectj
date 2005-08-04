import java.util.*;

public aspect AspectX {

  static Set matchedJps = new HashSet();
	
  before(): call(* Number.compareTo(..)) {
  	matchedJps.add(new String("call() matched on "+thisJoinPoint.toString()));
  }
  
  before(): execution(* Number.compareTo(..)) {
  	matchedJps.add(new String("execution() matched on "+thisJoinPoint.toString()));
  }
	
  public static void main(String []argv) {
  	Number n1 = new Number(5);
  	Number n2 = new Number(7);
  	n1.compareTo(n2);
    n1.compareTo("abc"); // A Java5 compiler would *not* allow this, a call to a bridge method: error should be:
    /**
       AspectX.java:19: compareTo(Number) in Number cannot be applied to (java.lang.String)
       n1.compareTo("abc");
          ^
       1 error
     */ 
  	
  	Iterator i = matchedJps.iterator();
  	while (i.hasNext()) {
  		String s = (String)i.next();
  		System.err.println(s);
  	}
  }
}
