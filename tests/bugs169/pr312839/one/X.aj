package com.wibble.foo;

import java.lang.annotation.*;

aspect X {
  public int Class.i;
  public String Class.getMeSomething() {
    return "abc";
  }
  declare parents: Class implements java.io.Serializable;
  declare @type: Class: @Foobar;
}

@Retention(RetentionPolicy.RUNTIME)
@interface Foobar {}
