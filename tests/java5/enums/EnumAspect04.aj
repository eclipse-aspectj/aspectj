import java.lang.Enum;

public aspect EnumAspect04 {
  interface I {};
  declare parents: SimpleE* implements I;

}
