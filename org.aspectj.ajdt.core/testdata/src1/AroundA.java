aspect AroundA {
	pointcut foo(): execution(void main(..));
	
	int around(int i, boolean b): args(i, b) && foo() {
		System.out.println("enter");
		return proceed(10, false);
	}
	
	
	void around(Object a): args(a) && foo() {
		System.out.println("enter");
		proceed("new: " + a);
		System.out.println("exit");
	}
	
	void around(final String[] a): args(a) && foo() {
		Runnable r = new Runnable() {
			public void run() {
				proceed(a);
			}
		};
		r.run();
		r.run();
	}
}
	
	