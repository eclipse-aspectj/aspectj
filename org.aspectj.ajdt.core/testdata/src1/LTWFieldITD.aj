import java.util.List;

public aspect LTWFieldITD {

	private int LTWHelloWorld.intField = 999;

	pointcut init (LTWHelloWorld hw) :
		execution(LTWHelloWorld.new()) && this(hw);
		
	after (LTWHelloWorld hw) : init (hw) {
		System.err.println("LTWFieldITD.init(" + thisJoinPointStaticPart + ")");
		hw.intField = 999999;
		hw.add(getClass().getName());
	}

}
