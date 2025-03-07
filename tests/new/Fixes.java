import org.aspectj.testing.Tester;

public class Fixes {
    public static void main(String[] args) { new Fixes().realMain(args); }
    public void realMain(String[] args) {
        new TheObject().go();
    }
}

class TheObject {
    private int private_int;
    void go () {}
}

privileged aspect TheAspect pertarget(target(TheObject)) {
    private TheObject theObject;

    /*
	after() returning(TheObject obj): execution(new()) {
		theObject = obj;
	}
	*/

    // XXX23: alternative to above advice which won't match
    before(TheObject o): execution(* go(..)) && target(o) {
    	theObject = o;
    }

    after() returning(): execution(* go(..)){//XXX23: changed from call(* realMain(..)) because I can't see how that could match with pertarget!
         start();
         postinc();
         preinc();
         postdec();
         predec();
     }

     void start() {
         theObject.private_int = 3;
     }
     
     void postinc() {
         enter("postinc");
         a(theObject.private_int,3);
         theObject.private_int++;
         a(theObject.private_int,4);
     }

     void preinc() {
         enter("preinc");
         a(theObject.private_int,4);
         ++theObject.private_int;
         a(theObject.private_int,5);
     }

     void postdec() {
         enter("postdec");
         a(theObject.private_int,5);
         theObject.private_int--;
         a(theObject.private_int,4);
     }

     void predec() {
         enter("predec");
         a(theObject.private_int,4);
         --theObject.private_int;
         a(theObject.private_int,3);
     }          

     private String msg;
     void enter(String msg) {
         this.msg = msg;
     }
     void a(int a, int b) {
//    	 System.out.println("Checking... "+a+"="+b);
         Tester.checkEqual(a,b,msg);
     }
}
