package fowl;

privileged aspect D {

    public void fish.PrivateClass.fooD() {
        d--;
        main.Main.doThang("D: " + d);
        main.Main.doThang("D: " + d());
    }
    
    before(fish.PrivateClass obj): call(void fish.PrivateClass.goo()) && target(obj) {
	obj.d--;
	main.Main.doThang("D: " + obj.d);
	main.Main.doThang("D: " + obj.d());
    }
}
