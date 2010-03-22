package a;

public class ClassUsingInner {
	
    class MyInner {}
    
    public void foo(MyInner i, Object h, String y) {}
    
    public void goo(ClassUsingInner.MyInner i, Object h, String y) {}
    
    public void hoo(a.ClassUsingInner.MyInner i, Object h, String y) {}
    
}