package fowl;
import fish.PrivateClass;
import main.Main;

privileged aspect C {
    
    public void PrivateClass.fooC() {
        c--;
        Main.doThang("C: " + c);
        Main.doThang("C: " + c());
    }
    
    before(PrivateClass obj): call(void PrivateClass.goo()) && target(obj) {
	obj.c--;
	Main.doThang("C: " + obj.c);
	Main.doThang("C: " + obj.c());
    }
}
