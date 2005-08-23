// should give *no* errors....
class Parent {
  // declared in Parent, overridden in Child
  public void parent_child() {} // AAA

  // declared in Parent, overridden in Injector
  public void parent_injector() {} // BBB
}

class Child extends Parent {
  // works
  @Override public void parent_child() {} // AAA

  // must override a superclass method
  @Override public void injector_child() {} // CCC
}

aspect Injector {
  public void Parent.injector_child() {} // CCC
  public void Parent.injector_injector() {} // DDD

  // must override a superclass method
  @Override public void Child.parent_injector() {} // BBB

  // must override a superclass method
  @Override public void Child.injector_injector() {} // DDD
}
