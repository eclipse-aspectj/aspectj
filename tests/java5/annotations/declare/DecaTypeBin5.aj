// Putting the wrong annotations onto types
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) @interface ColorT { String value();} 
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD) @interface ColorM { String value();} 
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.CONSTRUCTOR) @interface ColorC { String value();} 
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.ANNOTATION_TYPE) @interface ColorA { String value();} 
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) @interface ColorF { String value();} 
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.PARAMETER) @interface ColorP { String value();} 
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.LOCAL_VARIABLE) @interface ColorL { String value();} 
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.PACKAGE) @interface ColorPkg { String value();} 

public aspect DecaTypeBin5 {
  declare @type: A : @ColorT("Red");
  declare @type: A : @ColorM("Orange");
  declare @type: A : @ColorC("Yellow");
  declare @type: A : @ColorA("Green");
  declare @type: A : @ColorF("Blue");
  declare @type: A : @ColorP("Indigo");
  declare @type: A : @ColorL("Violet");
  declare @type: A : @ColorPkg("White");
}

aspect X {
  before(): execution(* *(..)) && @this(ColorT) { System.err.println("ColorT identified on "+thisJoinPoint); }
  before(): execution(* *(..)) && @this(ColorM) { System.err.println("ColorM identified on "+thisJoinPoint); }
  before(): execution(* *(..)) && @this(ColorC) { System.err.println("ColorC identified on "+thisJoinPoint); }
  before(): execution(* *(..)) && @this(ColorA) { System.err.println("ColorA identified on "+thisJoinPoint); }
  before(): execution(* *(..)) && @this(ColorF) { System.err.println("ColorF identified on "+thisJoinPoint); }
  before(): execution(* *(..)) && @this(ColorP) { System.err.println("ColorP identified on "+thisJoinPoint); }
  before(): execution(* *(..)) && @this(ColorL) { System.err.println("ColorL identified on "+thisJoinPoint); }
  before(): execution(* *(..)) && @this(ColorPkg) { System.err.println("ColorPkg identified on "+thisJoinPoint); }

}
