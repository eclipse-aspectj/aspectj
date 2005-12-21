import java.io.*;

aspect MyAspect {
    before(): execution(* MyOtherClass.read()) { }
}


class MyClass<T,E> implements MyInterface<T> {

    public static void main(String[] arg) { }

    public T read() throws IOException {
        return null;
    }

    public void exceptionDetected(E e) { }
}


interface MyInterface<T> {
    public T read() throws IOException;
}

class MyOtherClass {
    public void read() { }
}

public class pr121575 {
	public static void main(String []argv) {
		MyClass.main(null);
	}
}