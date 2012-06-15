import java.util.AbstractList;
import java.util.LinkedList;
import java.util.List;

abstract aspect FooAspectParent<T extends List> {
    protected int getNumber(int k) {
        return -1*k;
    }
}

abstract privileged aspect FooAspect<T extends AbstractList> extends FooAspectParent<T> {
    pointcut pc():  call(T.new()) && !within(Bar); 

    T around():pc() {
        System.out.println("superaspect getNumber returns "+getNumber(2)); 
        System.out.println("abstract method returns "+method());
		localMethod();
        Math.random(); //<-- works
        hashCode(); //<-- works
        return null;
    }    

    private void localMethod(){}

    protected abstract T method();
}

aspect Bar extends FooAspect<LinkedList> {
  protected LinkedList method() {
    System.out.println("Bar.method() running");
	return new LinkedList();
  }
}

public class Foo {

    public static void main(String[] argv) {
	  new Foo().bar();
    }
    public LinkedList bar() {
        new LinkedList();
        return null;
    }
}
