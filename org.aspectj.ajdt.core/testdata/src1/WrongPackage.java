package a.b.c;

public class WrongPackage {
    public static void main(String[] args) {
    	Runnable r = new Runnable() {
    		public void run() {}
    	};
    	r.run();
    }
}