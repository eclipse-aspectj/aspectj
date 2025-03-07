import java.util.*;

public aspect AspectX {

  static Set matchedJps = new HashSet();
	
  before(): execution(* Number.compareTo(..)) {
  	matchedJps.add(new String("execution() matched on "+thisJoinPoint.toString()));
  }
  
  public static void main(String []argv) {
  	Number n1 = new Number(5);
  	Number n2 = new Number(7);
	n1.compareTo(n2);
	
  	Iterator i = matchedJps.iterator();
  	while (i.hasNext()) {
  		String s = (String)i.next();
  		System.err.println(s);
  	}
  }
}
