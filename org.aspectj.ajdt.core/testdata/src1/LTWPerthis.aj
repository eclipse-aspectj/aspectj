import java.util.List;

public aspect LTWPerthis perthis(this(LTWHelloWorld)) {

	pointcut println (List list) :
		execution(* println()) && this(list);
	
	before (List list) : println (list) {
		System.err.println("LTWPerthis.println(" + thisJoinPointStaticPart + ")");
		list.add(getClass().getName());
	}

}
