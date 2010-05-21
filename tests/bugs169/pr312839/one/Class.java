package com.wibble.foo;

public class Class {
  private String name = "Andy";

  public static void main(String [] argv) {
    new Class().print();
  }

  public void print() {
    System.out.println("Hello "+name);
  }
}
