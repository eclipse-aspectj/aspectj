import java.io.IOException;

class DeclareSoft2 {
    public static void main(String[] x) {
	foo();
    }

    static void foo() throws IOException {
	throw new IOException();
    }

}

aspect A  {

    void around(): call(void foo()) {
	try { proceed(); }
	catch (IOException e) {}
    }
    declare soft: IOException: call(void foo());
}
