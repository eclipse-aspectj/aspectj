package de.rohith;
import java.lang.Object;

public aspect HelloWorldAspect {
    
	private int callDepth = -1;

    public HelloWorldAspect() {
    }
    
    pointcut hello(): !within(HelloWorldAspect);
    
    pointcut method(): execution(public (*[]) de..*(..));
    
    pointcut cloning(): call(* java.lang.Object.clone());

    declare warning: method() && hello(): "*[] returning method called" ;
    
    Object[] around(): cflow(method()) && cloning() && hello() {
    	print("", thisEnclosingJoinPointStaticPart);
    	Object[] ret = proceed(); 
    	return (Object[])ret.clone();
    }

    private void print(String prefix, Object message) {
        for (int i = 0, spaces = callDepth * 2; i < spaces; i++) {
            System.out.print(" ");
        }
        System.out.println(prefix + message);
    }
}
