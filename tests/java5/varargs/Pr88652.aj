public aspect Pr88652 {
	
	pointcut p(): call(Touple.new(..));
	
	declare warning: p() : "should match";
	
	public static void main(String[] args) {
		Touple t = new Touple(new Object());
		Touple t2 = new Touple(new Object(),new Object());
	}
}


class Touple {
	
	public Touple(Object formulaHandle, Object... propositions) {
		; // empty
	}
	
}