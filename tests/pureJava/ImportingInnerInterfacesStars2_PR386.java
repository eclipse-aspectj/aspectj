import org.aspectj.testing.*;
import ClassWithInnerInterfaces.Inner1;
import ClassWithInnerInterfaces.Inner1.*;

public class ImportingInnerInterfacesStars2_PR386 {
    public static void main(String[] args) {
        Tester.checkEqual(ClassWithInnerInterfaces.class.getName(),
                           "ClassWithInnerInterfaces");
        Tester.checkEqual(ClassWithInnerInterfaces.Inner1.class.getName(),
                           "ClassWithInnerInterfaces$Inner1");
        Tester.checkEqual(ClassWithInnerInterfaces.Inner1.Inner2.class.getName(),
                           "ClassWithInnerInterfaces$Inner1$Inner2");
        Tester.checkEqual(I1.class.getName(), "I1");
        Tester.checkEqual(I2.class.getName(), "I2");
    }
}

class I1 implements Inner1 {}
class I2 implements Inner2 {}
