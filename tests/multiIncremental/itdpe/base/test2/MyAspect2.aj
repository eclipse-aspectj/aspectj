package test2;

public aspect MyAspect2 {
    
    static interface Bar {
        
    }
    static class Foo {
        public Foo() {
            
        }
    }
    
    // must use fully qualified names
    declare parents : test.Demo implements test2.MyAspect2.Bar, Cloneable;
    
    // must use fully qualified names
    declare parents : test.Demo extends test2.MyAspect2.Foo;
    
    int Bar.bar() { 
        return 7;
    }
    
    int Foo.baz() {
        return 7;
    }
    
}
