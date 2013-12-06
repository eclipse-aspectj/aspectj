package com.foo.bar;

public class Test {

    abstract class X<T> {}

    class X1 extends X<Integer> {}

    class X2 extends X<String> {}

    public Test foo() {
        return this;
    }

    public <T> X<T> createMessage(int n) {
        X x;
        if (n == 0) {
            x = new X1();
        } else {
            x = new X2();
        }
        return x;
    }
    
    public static void main(String[] args) {
		
	}
}