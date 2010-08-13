//package org.springframework.persistence.test;

public abstract aspect AbstractAspect<S, T> {
 declare parents : Class extends S;
 declare parents : Class extends T;
}

aspect Aspect extends AbstractAspect<X, Y> { 
/* void something(X x) {
  something(new Class());
 }
 void something2(Y y) {
  something2(new Class());
 }*/
} 

interface X { }
interface Y { }

class Class {
 
 
}
