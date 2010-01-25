
public class ClassWithJoinPoint {
    public Boolean getValue() {
        return Boolean.FALSE;
    }

   public static void main(String[] arguments) {
/*
        System.out.println("Testing aspect style (should print \"true\"):");
        System.out.println(new aspect_style.ClassWithJoinPoint().getValue());
        
        System.out.println();
*/
        System.out.println("Testing annotation style (should print \"true\"):");
        System.out.println(new ClassWithJoinPoint().getValue());
    }
}
