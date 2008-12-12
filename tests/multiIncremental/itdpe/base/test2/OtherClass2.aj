package test2;

import test.Demo;

public class OtherClass2 {
    void x() {
        Demo d = new Demo(4);
        ((MyAspect2.Bar) d).bar();
        d.baz();
        
        ((Cloneable) d).toString();
    }
}
