public aspect Pr73050 {
	
	pointcut anonymousTypeMatchedByNamePattern() :
		staticinitialization(Pr73050.*1*);
	
	pointcut anonymousTypeMatchedByWildCard() :
		staticinitialization(Pr73050.*);
	
	declare warning : anonymousTypeMatchedByNamePattern() : 
		"anonymous types shouldn't be matched by name patterns";
	
	declare warning : anonymousTypeMatchedByWildCard() :
		"anonymous types should be matched by a * wild card";
	
	public void foo() {
		(new Runnable() {
			public void run() {}
		}).run();
	}
}