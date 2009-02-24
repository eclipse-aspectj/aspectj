package be.cronos.aop.aspects;
import java.io.*;
import be.cronos.aop.InterTypeAspectSupport;
import java.util.List;
public aspect InterTypeAspect {

    public interface InterTypeAspectInterface {
    }
    
    declare parents : (@InterTypeAspectSupport *) implements InterTypeAspectInterface;
    
    public String InterTypeAspectInterface.foo(int i,List list,Serializable a) {
    	return "bar";
    }

}
