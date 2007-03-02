package test;

public aspect ExecutionAspect {

declare parents: AbstractExecutable implements java.io.Serializable;

	pointcut executions(Executable executable): execution(public void Executable.execute()) && this(executable);
	
	void around(Executable executable): executions(executable) {
		System.err.println(thisJoinPoint);
	}
}
