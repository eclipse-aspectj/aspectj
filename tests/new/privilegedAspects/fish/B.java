package fish;
import main.Main;

privileged aspect B {
    private static String privateStatic = "B's private";

    //introduction PrivateClass {
    public void PrivateClass.fooB() {
        b--;
        Main.doThang("B: " + b);
        Main.doThang("B: " + b());

	System.out.println(privateStatic + "::" + FooC.privateStatic);
    }
    //}

    
    before(PrivateClass obj): call(void PrivateClass.goo()) && target(obj) {
	obj.b--;
	Main.doThang("B: " + obj.b);
	Main.doThang("B: " + obj.b());
    }
}

class FooC {
    private static int privateStatic = 2;
}
