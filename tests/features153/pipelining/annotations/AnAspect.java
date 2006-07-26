import java.lang.annotation.*;

public aspect AnAspect {

  declare @type: Foo: @SimpleAnnotation(id=5); // one type in an array


  declare @type: Foo: @AnnotationClassElement(clz=Integer.class); // one type not in an array


  before(): call(* (@SimpleAnnotation *).m(..))  {
  }

//  declare @type: Foo: @AnnotationStringElement(stringval="www"); // two types in an array
}
