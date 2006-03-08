package pkg;

public aspect A {

	pointcut p2(): call(* File.*(..));
}
