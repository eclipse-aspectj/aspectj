import java.lang.annotation.*;

public class A {
}

aspect X {
  declare @type: A: @Foo;
  declare parents: A implements java.io.Serializable;
//  declare @type: A: @Foo;
//  declare @type: A: @Foo;
  declare parents: A implements java.io.Serializable;
}

@Retention(RetentionPolicy.RUNTIME)
@interface Foo {}
