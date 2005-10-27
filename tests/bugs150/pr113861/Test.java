package com;
import java.util.*;

public class Test {

  public static void main(String [] argv) {
    new Test().foo();
  }

  Set<Integer> intsSet = new HashSet<Integer>();

  public Set<Integer> foo() {
    return intsSet;
  }
}
