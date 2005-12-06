public aspect PR118149 {

	public pointcut pc1(String s)
		: execution(* C.*()) && args(s) && if(s != null);
	
	public pointcut pc2(String s)
		: execution(C.new(String,..)) 
		&& args(s,..) && if(s != null);

	public pointcut pcOR(String s) : pc1(s) || pc2(s);
	
	before(String s) : pcOR(s) {	
	}
	
}


class C {

	public C(String s, boolean b) {		
	}
	
}
