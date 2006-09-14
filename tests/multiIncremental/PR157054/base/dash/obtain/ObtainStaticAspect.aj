package dash.obtain;


public aspect ObtainStaticAspect  {

	pointcut obtain_get(): get(@Obtain static * *);

	Object around(): obtain_get() {
		return proceed();
	}
	
	public void foo() {
		//System.out.println("foo: "+ObtainStaticTestClass.foo);
	}

}
