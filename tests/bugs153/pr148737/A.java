public abstract class A<E> {	
    public A() { }
    public void setUniqueID(Object o) {}
}

class B extends A {
    public B() {}    
}

class D {
    public void method() {
        B b = new B();
        b.setUniqueID(null);
    }
}

aspect TestAspect {
	before(): call(public void *.*()) { }    
}
