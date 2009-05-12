import java.lang.annotation.*;

public class F {
  int f;

  public static void main(String []argv) {
  }

}

aspect X {
  declare @field: @J * F.f: @I;
  declare @field: * F.f: @K;
  declare @field: @K * F.f: @J;
}

@Retention(RetentionPolicy.RUNTIME)
@interface I {}
@Retention(RetentionPolicy.RUNTIME)
@interface J {}
@Retention(RetentionPolicy.RUNTIME)
@interface K {}
