package fowl;
import main.Main;

privileged aspect D {

    public void fish.PrivateClass.fooD() {
        d--;
        Main.doThang("D: " + d);
        Main.doThang("D: " + d());
    }
    
    before(fish.PrivateClass obj): call(void fish.PrivateClass.goo()) && target(obj) {
	obj.d--;
	Main.doThang("D: " + obj.d);
	Main.doThang("D: " + obj.d());
    }
}
