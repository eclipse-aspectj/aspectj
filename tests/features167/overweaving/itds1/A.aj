package com.andy;

@interface Anno {}

aspect A {
  declare @type: com.andy.C: @Anno;

  int C.i = 5;

  public void C.m() {}

  before(): execution(* C.main(..)) {
    System.out.println("A:"+thisJoinPointStaticPart);
  }

}
