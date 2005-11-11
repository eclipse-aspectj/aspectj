public class PR113447 {

	public static void main(String[] args) {
		PR113447 me = new PR113447();
		me.method1();
		me.method3();
	}
	
	public void method1(){}

	public void method3(){}
}

aspect Super {

	// second method doesn't exist
	pointcut pc1(PR113447 s) : 
		(this(PR113447) && this(s) && execution(void method1()) && this(PR113447))
		|| (this(s) && execution(void method2()) && this(PR113447));

	before(PR113447 s) : pc1(s) {
	}
	
/*
	// second method does exist
	pointcut pc2(PR113447 s) : 
		(this(s) && execution(void method1()))
		|| (this(s) && execution(void method3()));

	before(PR113447 s) : pc2(s) {
	}
	
	// second method doesn't exist
	pointcut pc3(PR113447 s) : 
		(args(s) && execution(void method1()))
		|| (args(s) && execution(void method2()));

	before(PR113447 s) : pc3(s) {
	}
*/

}
