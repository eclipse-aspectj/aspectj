package pkg1;

public class Main extends pkg2.Foo {

    static class Noo extends Goo {
	//Noo() {}
    }

    public static void main(String[] args) {
	new Noo();
    }
}
