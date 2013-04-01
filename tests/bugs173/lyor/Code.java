import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Foo {}

aspect A {
  declare @method: void *.getName(): @Foo;
  public void Intface.getName() { }
}

interface Intface { }

class C implements Intface {}
