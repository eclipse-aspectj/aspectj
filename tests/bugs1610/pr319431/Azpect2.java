package com.foo.bar;

public aspect Azpect2 {
  before(): staticinitialization(*) {}
}
