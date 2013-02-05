
public class Bug2 {

    public static class ClassA2<T extends Interface12 & Interface22> implements java.io.Serializable {
    }

    public static class ClassB2 extends ClassA2<ClassB2> implements Interface12, Interface22 {
    }

    public interface Interface12 {
    }

    public interface Interface22 {
    }

    public static void main(String[] args) throws Exception {
        System.out.println(ClassB2.class.getGenericSuperclass());
    }
}
