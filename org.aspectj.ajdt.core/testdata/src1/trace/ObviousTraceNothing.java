package trace;

public aspect ObviousTraceNothing extends MegaTrace {
	pointcut where(): within(foo.bar..*);
	
	declare parents: foo.bar..* implements Marker;
}