package sample;

public abstract class Base implements Iface {
    int x;
    Base() {
	x=1;
    }
    abstract void foo();
}
