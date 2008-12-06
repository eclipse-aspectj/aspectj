package example;
import impl.*;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareParents;

public class Main2 {
    public static void main(String[] args) {
        Bar bar = new Bar();
        ((Foo)bar).doFoo();
	bar.doBar();
    }
}

class Bar {
    public void doBar() {
        System.out.println("Bar");
    }
}

@Aspect
class Introduce {
    @DeclareParents(value="example.Bar", defaultImpl=impl.DefaultFoo.class)
    private Foo mixin;
}

