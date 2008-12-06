package impl;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareParents;

public class DefaultFoo implements Foo {
//    Uncommenting the following fixes the error    
    DefaultFoo() {
    }
    public void doFoo() {
        System.out.println("In doFoo " + this.getClass());
    }
}
