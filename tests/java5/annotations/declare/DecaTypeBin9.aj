import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) @interface ColorT { String value();} 
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.ANNOTATION_TYPE) @interface ColorA { String value();} 

public aspect DecaTypeBin9 {
  declare @type: A* : @ColorT("Red");
  declare @type: A* : @ColorA("Green");
}

