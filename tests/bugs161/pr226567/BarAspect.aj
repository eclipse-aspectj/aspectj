package a;

import b.Bar;
import b.Foo;

public aspect BarAspect {
   
   declare parents : Foo implements Bar;
}