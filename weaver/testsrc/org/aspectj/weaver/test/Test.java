package org.aspectj.weaver.test;

public class Test {
    public static void main(String[] args) {
        foo()
        .
        foo();
    }
    public static Test foo() {
        new Exception().printStackTrace();
        return new Test();
    }
}
