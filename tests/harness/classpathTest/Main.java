
import org.aspectj.testing.Tester;
import jar.required.Global;
import classfile.required.ClassFile;

public class Main {
    public static void main (String[] args) {
        Tester.check(ClassFile.isTrue(), "not ClassFile.isTrue()?");
        Tester.check(Global.isTrue(), "not Global.isTrue()?");
    } 
}
