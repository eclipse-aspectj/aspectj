import java.lang.annotation.*;

enum SimpleEnum { Red,Orange,Yellow,Green,Blue,Indigo,Violet };

@Retention(RetentionPolicy.RUNTIME)
@interface AnnotationEnumElement {
  SimpleEnum enumval();
}


@AnnotationEnumElement(enumval=SimpleEnum.Red)
class C {
}
