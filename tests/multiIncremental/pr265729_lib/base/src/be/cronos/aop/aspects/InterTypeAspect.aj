package be.cronos.aop.aspects;

import be.cronos.aop.InterTypeAspectSupport;

public aspect InterTypeAspect {

    public interface InterTypeAspectInterface {
    }
    
    declare parents : (@InterTypeAspectSupport *) implements InterTypeAspectInterface;
    
    public String InterTypeAspectInterface.foo(int i) {
    	return "bar";
    }

}
