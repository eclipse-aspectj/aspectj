// bug 44272
public aspect CircularAdvicePrecedence {
    pointcut crun() : execution (void run()) ;
    before() : crun() {}
    after() returning : crun() {}
    void around() : crun() { proceed(); }
}

class Runner {
	
	public void run() {}
	
}