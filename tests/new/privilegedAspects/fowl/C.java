package fowl;
import fish.*;

privileged aspect C {
    
    public void fish.PrivateClass.fooC() {
        c--;
        main.Main.doThang("C: " + c);
        main.Main.doThang("C: " + c());
    }
    
    before(PrivateClass obj): call(void PrivateClass.goo()) && target(obj) {
	obj.c--;
	main.Main.doThang("C: " + obj.c);
	main.Main.doThang("C: " + obj.c());
    }
}
