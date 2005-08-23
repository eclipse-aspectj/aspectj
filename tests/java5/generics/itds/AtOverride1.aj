// 1. child specifies override but there was no parent (error)

class Parent {

}

class Child extends Parent {

  @Override public void method() {} // ERROR, doesnt override anything

}

aspect Injector { }
