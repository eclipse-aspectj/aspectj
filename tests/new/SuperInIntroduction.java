
import org.aspectj.testing.Tester;

public class SuperInIntroduction {
    public static void main (String[] args) {
        int result = new Sub().getInt();
        Tester.check(8==result, "new Sub().getInt() !8==" + result);
        ObjectSub sb = new ObjectSub().getClone();
        Tester.check(null != sb, "null new ObjectSub().getClone()");
        sb = new ObjectSub().getSuperClone();
        Tester.check(null != sb, "null new ObjectSub().getSuperClone()");
    } 
}

class Super {
    protected int protectedInt = 1;
    protected int protectedIntMethod() { return protectedInt; }
    int defaultInt = 1;
    int defaultIntMethod() { return defaultInt; }
}

class Sub extends Super { }
class ObjectSub { }

aspect A {
    /** @testcase accessing protected method and field of class within code the compiler controls */
    public int Sub.getInt() {
        int result;
        result = super.protectedInt;
        result += super.protectedIntMethod();
        result += protectedInt;
        result += protectedIntMethod();
        result += defaultInt;
        result += defaultIntMethod();
        result += super.defaultInt;
        result += super.defaultIntMethod();
        return result;
    }

    /** @testcase accessing protected method of class outside code the compiler controls */
    public ObjectSub ObjectSub.getClone() {
         try {
            Object result = clone();
            return (ObjectSub) result;
         } catch (CloneNotSupportedException e) {
             return this;
         }
    } 

    /** @testcase using super to access protected method of class outside code the compiler controls */
    public ObjectSub ObjectSub.getSuperClone() {
        ObjectSub result = null;
        try {
            result = (ObjectSub) super.clone();
            Tester.check(false, "expecting CloneNotSupportedException"); 
        } catch (CloneNotSupportedException e) {
            result = this; // bad programming - ok for testing
        }
        return result;
    }
}
