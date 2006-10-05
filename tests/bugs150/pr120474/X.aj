public aspect X {
  before(): execution(* foo(..)) && !within(X) { 
	  System.out.println(thisJoinPoint);
  }
}
