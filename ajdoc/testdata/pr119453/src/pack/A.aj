package pack;

public aspect A {

	private int C.y = 3;

	declare warning : execution(* C.method()) : "warning..";
	
	/**
	 * blah
	 */
	public String C.m(){return "";};

	public C.new(String s){ this();};
	
	pointcut p() : execution(* *.*(..));
	
	before() : p() {
	}
	
	pointcut p1() : execution(public String C.method1(..));
	after() returning : p1() {
	}
}
