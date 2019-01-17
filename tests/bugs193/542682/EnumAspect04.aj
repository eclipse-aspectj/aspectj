import java.lang.annotation.*;
import java.lang.Enum;

public aspect EnumAspect04 {
  interface I {};
  //declare parents: SimpleE* implements I;
  //declare parents: !*Aspect04 implements I;
  declare parents: @Foo * implements I;
}

@Retention(RetentionPolicy.RUNTIME)
@interface Foo {}
