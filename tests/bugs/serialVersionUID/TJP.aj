public aspect TJP {
	
	pointcut run () :
		execution(public void *.run());
	
	before () : run () {
		System.out.println("? TJP.execRun() thisJoinPointStaticPart=" + thisJoinPointStaticPart);
	}
	
	pointcut write () :
		call(public void *.run());
	
	before () : write () {
		System.out.println("? TJP.callRun() thisJoinPointStaticPart=" + thisJoinPointStaticPart);
	}
	
	before () : write () {
		System.out.println("? TJP.callRun() thisEnclosingJoinPointStaticPart=" + thisEnclosingJoinPointStaticPart);
	}
	
	pointcut getI () :
		get(int i);

	before () : getI () {
		System.out.println("? TJP.getI() thisJoinPoint=" + thisJoinPoint);
	}
	
	pointcut setI () :
		set(int i);

	before () : setI () {
		System.out.println("? TJP.setI() thisJoinPoint=" + thisJoinPoint);
	}
}