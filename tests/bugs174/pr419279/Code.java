public aspect Code {
  before(): execution(* *(String)) { }
  before(): call(* someMethod(..)) {
		System.out.println(thisJoinPoint);
  }
  public void foo() {
    someMethod();
  }
  public void someMethod(){}
}
