package com.foo.goo;
import java.util.List;
import java.lang.annotation.Retention;
import org.aspectj.lang.annotation.Around;

public class C {
  public static void main(String []argv) {
    new C().foo();
  }

  public void foo() {}
}
