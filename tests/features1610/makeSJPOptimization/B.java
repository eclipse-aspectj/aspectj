public class B{
	public static void main(String args[]) throws Throwable{
		B b = new B();
		b.method1(null);
		b.method2(null,null);
		b.method3();
		method4();

	}


	
	public Object method1(String p1){
		return "Hola";
	}
	
	public Object method2(String p1, Integer p2) throws Exception{
		return "Hola";
	}
	
	private void method3(){
		return;
	}

	public  static void method4(){
		return;
	}

}
