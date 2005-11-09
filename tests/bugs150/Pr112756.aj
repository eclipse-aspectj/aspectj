
public aspect Pr112756 {
  private ThreadLocal counts = new ThreadLocal();

  public pointcut testMethodExecution() : 
    execution(void Test+.test*());

  public pointcut assertCall() : 
    cflow(testMethodExecution()) && call(void Assert+.assert*(..));

  void around() : testMethodExecution() {
    counts.set( new Counter());
  
    proceed();
  
    if(((Counter) counts.get()).getCount()==0) {
      throw new RuntimeException("No assertions had been called");
    }
  }

  before() : assertCall() {
    ((Counter) counts.get()).inc();
  }
  
}

class Assert {
	
	public static boolean assertEquals() { return true; }
	public static boolean assertSame() { return true; }
	
}

class Test {
	
	public void testFoo() {}
	
}

class Counter {
	
	int count;
	
	public void inc() { count++; }
	public int getCount() { return count; }
	
}