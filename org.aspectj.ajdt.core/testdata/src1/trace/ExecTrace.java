package trace;

public aspect ExecTrace extends MegaTrace {
	pointcut where(): execution(* *(..));
	
	declare parents: !Object implements Marker;
}