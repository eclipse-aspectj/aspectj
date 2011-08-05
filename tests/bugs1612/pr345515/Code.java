import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {
  String string();
}

@Retention(RetentionPolicy.RUNTIME)
@interface Anno2 {
  String string() default "abc";
}


public class Code {

  @Anno(string="hello")
  int field;

  public static void main(String []argv) {
  }

}

aspect X {
  declare @field: @Anno(string=$1) * *: @Anno2(string=$1);
}
