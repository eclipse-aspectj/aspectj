class WarningSample {

	public void method() {}
	  public void anotherMethod() {
	    this.method();
	  }

	}

	aspect WarningAspect {

	  pointcut illegalCall(): call(* WarningSample.method())
	                && within(WarningSample);

	  // the same thing happens with declare error
	  declare warning: illegalCall() : "Hey, don't " +
	      "do that, that is not nice. You should do something else";

	  public void e1() {}
	  declare warning: execution(* e1(..)): "hello " + /* comment */ "world"

	  public void e2() {}
	  declare warning: execution(* e2(..)): "hello " /* comment + "world";
	  
	  public void e3() {}
	  declare warning: execution(* e3(..)): "hello " + // commenthere
	
	}

