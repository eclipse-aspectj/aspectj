package trace;

public aspect HardTraceNothing extends MegaTrace {
	pointcut where(): call(public * frotz(..));
}