import java.io.IOException;

public class ExplicitConstructorThrows extends Base { //ERR: default constructor throws IOException
}

class Base {
    Base() throws IOException { }
}

class Sub1 extends Base {
    Sub1() {
        super(); //ERR: throws IOException
    }
}

class Sub2 extends Base {
    Sub2(String s) {
        this();  //ERR: throws IOException
    }

    Sub2() throws IOException {
        super();
    }
}
