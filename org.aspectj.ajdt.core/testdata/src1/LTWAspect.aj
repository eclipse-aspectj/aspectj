import java.util.List;

public privileged aspect LTWAspect {
	
	pointcut method (List list) :
		execution(* LTWHelloWorld.*(..)) && this(list);
	
	before (List list) : method (list) {
		System.err.println("LTWAspect.method(" + thisJoinPointStaticPart + ")");
		list.add("LTWAspect");
	}
}
