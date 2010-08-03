package com.foo;

public class Extender extends Super implements Marker {
  public void aMethod(String aString) {}
  public int aField;

}

class Super {}

interface Marker {}
