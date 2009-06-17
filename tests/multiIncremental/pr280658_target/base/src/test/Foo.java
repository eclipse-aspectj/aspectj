package test;
import java.util.Arrays;
public class Foo {
    public static void main(String[] args) {
        System.out.println(Arrays.toString(new
Foo().getClass().getInterfaces()));
        ((Runnable) new Foo()).run();
    }
}


