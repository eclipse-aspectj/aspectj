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
     after() returning(TheObject obj): call(new()) {
          theObject = obj;
     }
     after() returning(): call(* realMain(..)) {
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
         Tester.checkEqual(a,b,msg);
     }
}
