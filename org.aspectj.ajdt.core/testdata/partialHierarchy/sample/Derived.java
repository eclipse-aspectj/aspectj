package sample;

public class Derived extends Base {
    int y;
    public Derived() {
	super();
	y = 2;
    }

    public void foo() {}

    public static void main(String args[]) {
	new Derived();
    }
}
