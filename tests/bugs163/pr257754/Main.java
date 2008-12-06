package example;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareParents;

public class Main {
    public static void main(String[] args) {
        Bar bar = new Bar();
        ((Foo)bar).doFoo();
	bar.doBar();
    }
}

interface Foo {
    public void doFoo();
}

class DefaultFoo implements Foo {
//    Uncommenting the following fixes the error    
//    public DefaultFoo() {
//    }
    public void doFoo() {
        System.out.println("In doFoo " + this.getClass());
    }
}

class Bar {
    public void doBar() {
        System.out.println("Bar");
    }
}

@Aspect
class Introduce {
    @DeclareParents(value="example.Bar", defaultImpl=DefaultFoo.class)
    private Foo mixin;
}

