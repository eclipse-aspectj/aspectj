public class PR113447e {

	public static void main(String[] args) {
		PR113447e me = new PR113447e();
		me.method1(1);
		me.method3(2);
	}
	
	public void method1(int i){}

	public void method3(int i){}
}

aspect Super {

	// second method doesn't exist
	pointcut pc1(int i) : 
		(args(i) && call(void method1(int)))
		|| (args(i) && call(void method2(int)));

	before(int i) : pc1(i) {}
	
	// second method does exist
	pointcut pc2(int i) : 
		(args(i) && call(void method1(int)))
		|| (args(i) && call(void method3(int)));

	before(int i) : pc2(i) {}
	
	// ensure this still works
	pointcut pc3(int i) :
		args(i) && (call(void method1(int)) || call(void method2(int)));
	
	before(int i) : pc3(i) {}
	after(int i) : pc3(i) {}
}
