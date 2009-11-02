package pkg;

import java.lang.annotation.*;

class A {public void m() {}}

class B extends A {public void m() {}}

class C extends B {public void m() {}}

aspect X {
  pointcut p(): execution(* C.*(..));
  before(): p() {}
}

@Retention(RetentionPolicy.RUNTIME)
@interface Marker {}
