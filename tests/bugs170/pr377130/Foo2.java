import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {}

@Anno
privileged aspect Foo2 {
  public static void main(String []argv) {
    System.out.println(Foo2.class.getAnnotation(Anno.class));
  }
}
