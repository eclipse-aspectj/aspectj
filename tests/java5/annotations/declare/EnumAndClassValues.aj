import java.lang.annotation.*;

enum SimpleEnum { Red,Orange,Yellow,Green,Blue,Indigo,Violet };

@Retention(RetentionPolicy.RUNTIME)
@interface AnnotationEnumElement {
  SimpleEnum enumval();
}

@Retention(RetentionPolicy.RUNTIME)
@interface AnnotationClassElement {
  Class clz();
}


aspect AnnotatedWithEnumClass {
  declare @type: FunkyAnnotations : @AnnotationEnumElement(enumval=SimpleEnum.Red);
  declare @type: FunkyAnnotations : @AnnotationClassElement(clz=Integer.class);

  before(AnnotationEnumElement aee): call(* *(..)) && @target(aee) {
    System.err.println("advice running: "+aee.enumval());
  }

  before(AnnotationClassElement ace): call(* *(..)) && @target(ace) {
    System.err.println("advice running: "+ace.clz());
  }
}
