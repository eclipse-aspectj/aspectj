package pkg;

public aspect AspectInPackage {

	pointcut p() : execution(* *.*(..));

	before() : p() {
	}

}
