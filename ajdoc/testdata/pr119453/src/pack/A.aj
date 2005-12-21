package pack;

public aspect A {

	private int C.y = 3;

	/**
	 * blah
	 */
	public String C.m(){return "";};

	public C.new(String s){ this();};
	
	pointcut p() : execution(* *.*(..));
	
	before() : p() {
	}
	
}
