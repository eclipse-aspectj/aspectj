
aspect X {
  declare parents: Bug.ClassA implements java.io.Serializable;
}
public class Bug {

//    @org.springframework.beans.factory.annotation.Configurable
    public static class ClassA<T extends Interface1 & Interface2> {
    }

    public static class ClassB extends ClassA<ClassB> implements Interface1, Interface2 {
    }

    public interface Interface1 {
    }

    public interface Interface2 {
    }

    public static void main(String[] args) throws Exception {
        System.out.println(ClassB.class.getGenericSuperclass());
    }
}
