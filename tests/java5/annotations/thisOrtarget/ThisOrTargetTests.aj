import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import org.aspectj.lang.JoinPoint.StaticPart;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

public aspect ThisOrTargetTests {
	
  List<String> before1Matches = new ArrayList<String>();
  List<String> before2Matches = new ArrayList<String>();
  List<String> after1Matches = new ArrayList<String>();
  List<String> after2Matches = new ArrayList<String>();
	
	
  pointcut doSomethingExecution() : execution(* doSomething());
  pointcut doSomethingCall() : call(* doSomething());
  
  before() : doSomethingExecution() && @this(MyAnnotation) {
  	// should match:
  	// b.doSomething(), reallyB.doSomething() [with test],
  	// c.doSomething()
  	add(before1Matches,thisJoinPointStaticPart);
  }
  
  before() : doSomethingExecution() && @this(MyInheritableAnnotation) {
  	// should match:
  	// c.doSomething()
  	// d.doSomething()
  	// reallyD.doSomething()
  	add(before2Matches,thisJoinPointStaticPart);
  }
  
  after() returning : doSomethingCall() && @target(MyAnnotation) {
  	// should match:
  	// b.doSomething(), reallyB.doSomething() [with test],
  	// c.doSomething()
  	add(after1Matches,thisJoinPointStaticPart);
  }
  
  after() returning : doSomethingCall() && @target(MyInheritableAnnotation) {
  	// should match:
  	// c.doSomething()
  	// d.doSomething()
  	// reallyD.doSomething()
  	add(after2Matches,thisJoinPointStaticPart);
  }
  	
  private void add(List<String> toList, JoinPoint.StaticPart jpsp) {
  	Signature sig = jpsp.getSignature();
  	String toAdd = sig.getDeclaringTypeName() + "." + sig.getName(); 
  	toList.add(toAdd);
  }
  
  after() returning : execution(* main(String[])) {
  	assertMatches("before1",before1Matches,
  			new String[] {"B.doSomething","C.doSomething","B.doSomething"} );  	
  	assertMatches("before2",before2Matches,
  			new String[] {"C.doSomething","D.doSomething","D.doSomething"} );  	
  	assertMatches("after1",after1Matches,
  			new String[] {"B.doSomething","C.doSomething","A.doSomething"} );  	
  	assertMatches("after2",after2Matches,
  			new String[] {"C.doSomething","D.doSomething","C.doSomething"} );  	
  }
  
  private void assertMatches(String name, List<String> matches,String[] spec) {
  	if (matches.size() != spec.length) {
  		for (Iterator<String> iter = matches.iterator(); iter.hasNext();) {
			String match = iter.next();
			System.out.println(match);
		}
  		
 		throw new RuntimeException(name + ": Expected " + spec.length + " matches, got " + matches.size());
  	}
  	for (int i = 0; i < spec.length; i++) {
		if (!matches.get(i).equals(spec[i])) throw new RuntimeException(name + ":Excepted " + spec[i] + " got " + matches.get(i));
	}
  }
}