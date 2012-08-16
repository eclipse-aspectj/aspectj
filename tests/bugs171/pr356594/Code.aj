import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Foo {}
@Retention(RetentionPolicy.RUNTIME)
@interface Bar {}

class C {
}

aspect X {
declare @type: C: @Foo @Bar;
}

