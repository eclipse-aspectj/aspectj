package p.q.r;
import java.lang.annotation.*;

aspect Asp {

declare parents: C implements p.q.r.Int;
declare parents: C implements Int;
declare @type: C: @Foo;
declare @type: C: @p.q.r.Goo;

declare @field: int C.i: @Foo;
declare @method: void C.m(): @Foo;
declare @constructor: new(): @Foo;

}

@Retention(RetentionPolicy.RUNTIME) @interface Foo {}
@Retention(RetentionPolicy.RUNTIME) @interface Goo {}

interface Int {}

class C {
  int i;
  void m() {}
  C() {}
}
