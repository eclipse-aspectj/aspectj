import java.lang.annotation.*;

aspect X {
declare parents: 
  @SomeAnnotation(a = @Foo) * implements java.io.Serializable;
}

  @SomeAnnotation(a = @Foo)
  public class Example { 

public static void main(String []argv) {
  Example e = new Example();
if (e instanceof java.io.Serializable) {
System.out.println("yes");
} else {
System.out.println("no");
}
}

}

@Retention(RetentionPolicy.RUNTIME)
@interface Foo {}

@Retention(RetentionPolicy.RUNTIME)
@interface SomeAnnotation {
  Foo a();
}

