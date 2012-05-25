import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {}

@Anno
aspect Foo {
  public static void main(String []argv) {
    System.out.println(Foo.class.getAnnotation(Anno.class));
  }
}
