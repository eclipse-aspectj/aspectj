package fish;

public class PrivateClass {
    private int a = 999;
    private int a() { return 888; }
    private int b = 777;
    private int b() { return 666; }
    private int c = 555;
    private int c() { return 444; }
    private int d = 333;
    private int d() { return 222; }

    public void goo() {}
}

privileged aspect A {
    
    public void PrivateClass.fooA() {
        a--;
        main.Main.doThang("A: " + a);
        main.Main.doThang("A: " + a());
    }
    
    before(PrivateClass obj): call(void PrivateClass.goo()) && target(obj) {
	obj.a--;
	main.Main.doThang("A: " + obj.a);
	main.Main.doThang("A: " + obj.a());
    }
}


