import java.util.AbstractList;
import java.util.LinkedList;
import java.util.List;

abstract aspect FooAspectParent<T extends List> {
    protected int getNumber(int k) {
        return -1*k;
    }
}

abstract privileged aspect FooAspect<T extends AbstractList> extends FooAspectParent<T> {
    pointcut pc():  call(T.new()); 

    T around():pc() {
        //getNumber(1); //<-- method call to superAspect fails   
        method();  // <-- method call to abstract local defined method fails
        //localMethod(); //<-- method call to local private method fails
        Math.random(); //<-- works
        hashCode(); //<-- works
        return null;
    }    

    private void localMethod(){}

    protected abstract T method();
}

/*
class Foo {
    public LinkedList bar() {
        new LinkedList();
        return null;
    }
}
*/
