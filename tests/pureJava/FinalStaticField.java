
import java.lang.reflect.*;
import org.aspectj.testing.Tester;


class C{}

class D {
    public static final C public_c = new C();
    private static final C private_c = new C();
    protected static final C protected_c = new C();
    static final C default_c = new C();
}

/** @testcase PR#866 final static fields not marked as such in binaries */
public class FinalStaticField {
    public static final String[] FIELDS = new String[]
        { "public_c", "private_c", "protected_c", "default_c" };
    
    public static void main(String[] args) {

        Tester.expectEvents(FIELDS);

        Field[] fields = D.class.getDeclaredFields();
        StringBuffer failures = new StringBuffer();
        for (int i = 0; i < fields.length; i++) {
            String fieldName = fields[i].getName();
            Tester.event(fieldName);
			if (!fieldName.endsWith("_c")) {
                Tester.check(false, "unexpected field: " + fieldName);
            } else if (!(Modifier.isFinal(fields[i].getModifiers()))) {
                failures.append(fieldName + " ");
            }
        }
        Tester.checkAllEvents();
        if (0 < failures.length()) {
            Tester.check(false, failures.toString());
        }
    }
}
