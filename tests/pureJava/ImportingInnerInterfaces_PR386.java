import org.aspectj.testing.*;
import ClassWithInnerInterfaces.Inner1;
import ClassWithInnerInterfaces.Inner1.Inner2;
import ClassWithInnerInterfaces.Inner1.Inner2.Inner3;
public class ImportingInnerInterfaces_PR386 {
    public static void main(String[] args) {
        Tester.checkEqual(ClassWithInnerInterfaces.class.getName(),
                           "ClassWithInnerInterfaces");
        Tester.checkEqual(ClassWithInnerInterfaces.Inner1.class.getName(),
                           "ClassWithInnerInterfaces$Inner1");
        Tester.checkEqual(ClassWithInnerInterfaces.Inner1.Inner2.class.getName(),
                           "ClassWithInnerInterfaces$Inner1$Inner2");
        Tester.checkEqual(ClassWithInnerInterfaces.Inner1.Inner2.Inner3.class.getName(),
                           "ClassWithInnerInterfaces$Inner1$Inner2$Inner3");
        Tester.checkEqual(I1.class.getName(), "I1");
        Tester.checkEqual(I2.class.getName(), "I2");
        Tester.checkEqual(I3.class.getName(), "I3");
    }
}

class I1 implements Inner1 {}
class I2 implements Inner2 {}
class I3 implements Inner3 {}
