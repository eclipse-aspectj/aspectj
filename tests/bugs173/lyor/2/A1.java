import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Foo {}

aspect A1 {
  declare @method: void Intface+.getName(): @Foo;
}

