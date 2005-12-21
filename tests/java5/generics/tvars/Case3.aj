import java.lang.annotation.*;
import java.io.*;

aspect MyAspect {
    before(): execution(* read(..)) { }
}


class MyClass<T extends String,E extends Number> implements MyInterface<T> {

    public static void main(String[] arg) { }

    public void read(E e) throws IOException {
    }

    public void exceptionDetected(E e) { }
}


interface MyInterface<T> {
    public void read(T t) throws IOException;
}

