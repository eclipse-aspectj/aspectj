package apackage;

public aspect InitCatcher {
	
	declare warning: call(* *.init(..)) :
		"Please don't call init methods";	

	declare warning: set(* SomeClass.*) :
		"Please don't call setters";	
	
}