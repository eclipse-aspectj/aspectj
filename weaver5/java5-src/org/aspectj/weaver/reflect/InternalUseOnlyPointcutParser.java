package org.aspectj.weaver.reflect;

import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.tools.PointcutParameter;
import org.aspectj.weaver.tools.PointcutParser;

public class InternalUseOnlyPointcutParser extends PointcutParser {

	public InternalUseOnlyPointcutParser(ClassLoader classLoader, ReflectionWorld world) {
		super();
		setClassLoader(classLoader);
		setWorld(world);
	}
	
	public InternalUseOnlyPointcutParser(ClassLoader classLoader) {
		super();
		setClassLoader(classLoader);
	}
	
	public Pointcut resolvePointcutExpression(
	    		String expression, 
	    		Class inScope,
	    		PointcutParameter[] formalParameters) {
		return super.resolvePointcutExpression(expression, inScope, formalParameters);
	}
	
	public Pointcut concretizePointcutExpression(Pointcut pc, Class inScope, PointcutParameter[] formalParameters) {
		return super.concretizePointcutExpression(pc, inScope, formalParameters);
	}
		   
}
