package trace;

public aspect ExecTrace extends MegaTrace {
	pointcut where(): execution(public * *(..));
	
	declare parents: * implements Marker;
}