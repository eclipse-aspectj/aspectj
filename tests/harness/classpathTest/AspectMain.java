
import org.aspectj.testing.Tester;
import jar.required.Global;
import classfile.required.ClassFile;

public class AspectMain {
    public static void main (String[] args) {
        Tester.expectEvent("aspect");
        Tester.check(ClassFile.isTrue(), "not ClassFile.isTrue()?");
        Tester.check(Global.isTrue(), "not Global.isTrue()?");
        // aspect advises this invocation, adds "aspect" event
        new Runnable() { public void run(){}}.run();
        Tester.checkAllEvents();
    } 
}
