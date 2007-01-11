
public aspect AspectDeclareSoft {
	
  declare soft: MyException: execution(* main(..));

  declare soft: Exception+: execution(* main(..));

}
class MyException extends Exception {}
