import java.util.List;

public privileged aspect LTWMethodITD {

	public String LTWHelloWorld.getMessage () {
		return message;
	}
	
	public void LTWHelloWorld.setMessage (String newMessage) {
		message = newMessage;
	}

	pointcut init (LTWHelloWorld hw) :
		execution(LTWHelloWorld.new()) && this(hw);
		
	after (LTWHelloWorld hw) : init (hw) {
		System.err.println("LTWMethodITD.init(" + thisJoinPointStaticPart + ")");
		hw.getMessage();
		hw.setMessage("Hello LTWMethodITD");
		hw.add(getClass().getName());
	}
}