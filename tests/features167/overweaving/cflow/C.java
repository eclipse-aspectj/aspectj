package com.andy;

public class C {
  public String name = "andy";

  public static void main(String []argv) {
    new C().run();
  }

  public void run() {
    System.out.println("hello "+name);
  }
}
