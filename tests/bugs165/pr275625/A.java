import java.lang.annotation.*;

public class A {
  public void m() {}

  public static void main(String []argv) {
  }

}

aspect X {
  declare @method: @J * A.m(): @I;
  declare @method: * A.m(): @K;
  declare @method: @K * A.m(): @J;
}

@Retention(RetentionPolicy.RUNTIME)
@interface I {}
@Retention(RetentionPolicy.RUNTIME)
@interface J {}
@Retention(RetentionPolicy.RUNTIME)
@interface K {}
