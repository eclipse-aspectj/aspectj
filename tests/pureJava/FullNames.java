import org.aspectj.testing.Tester;
import org.aspectj.testing.Tester; 
public class FullNames {
    public static void main(String[] args) {
        new FullNames().realMain(args);
    }
    public void realMain(String[] args) {
        a();
        b();
        c();
        Tester.checkAllEvents();
    }
    static {
        Tester.expectEvent("a");
        Tester.expectEvent("b");
        Tester.expectEvent(String.class.toString()); // XXX changed w/o understanding
    }
    private void a() {
        String java = new String("a");
        Tester.event(java);
    }
    public void b() {
        String java = String.copyValueOf(new char[]{'b'});
        Tester.event(java);
    }
    public void c() {
        Class java = String.class;
        Tester.event(java.toString()); // XXX changed w/o understanding
    }
}
