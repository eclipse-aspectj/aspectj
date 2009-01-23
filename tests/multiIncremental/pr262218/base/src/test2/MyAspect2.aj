package test2;

import test.Demo;
import test2.MyAspect2;

public aspect MyAspect2 {
    
    static interface Bar {
        
    }
    declare parents : Demo implements Bar, Cloneable;
    
    public int Bar.bar() { 
        return 7;
    }

    
    static class Foo {
        public Foo() {
             
        } 
    }
    declare parents : Demo extends Foo;
    
    public int Foo.baz() {
    	return 7;
    } 

}
