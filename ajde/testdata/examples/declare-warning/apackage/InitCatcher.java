package apackage;

public aspect InitCatcher {
	
	declare warning: call(* *.init(..)) :
		"Please don't call init methods";	//$NON-NLS-1$
	
}