package com.foo.bar;

public aspect Azpect {
  before(): staticinitialization(*){}
}
