import java.util.List;

public privileged aspect LTWInterfaceITD {

	declare parents : LTWHelloWorld implements Runnable;
	
	public void LTWHelloWorld.run () {
		add("LTWInterfaceITD");
	}

	pointcut init (LTWHelloWorld hw) :
		execution(LTWHelloWorld.new()) && this(hw);
		
	after (LTWHelloWorld hw) : init (hw) {
		System.err.println("LTWInterfaceITD.init(" + thisJoinPointStaticPart + ")");
		hw.run();
	}
}
