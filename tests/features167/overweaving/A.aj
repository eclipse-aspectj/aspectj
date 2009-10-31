package com.andy;

@interface Anno {}

aspect A {
  declare @type: com.andy.C: @Anno;

  before(): execution(* C.main(..)) {
    System.out.println("AspectA>>"+thisJoinPointStaticPart);
  }

}
