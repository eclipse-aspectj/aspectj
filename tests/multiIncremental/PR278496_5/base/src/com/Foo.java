package com;

import java.util.*;

public class Foo {

  public int i;
  public String s;

  @Anno
  public List<String> ls;
 
  @Anno2(a=42) @Anno
  public List<Integer> li;
}
