// 5. child placed by ITD, @override on child, no parent (error)

class Parent {
}

class Child extends Parent {
}

aspect Injector { 

  @Override public void Child.method() {}  // ERROR, no parent

}
