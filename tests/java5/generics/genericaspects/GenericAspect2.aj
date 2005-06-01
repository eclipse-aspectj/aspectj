// illegal, super aspect is not abstract

public aspect GenericAspect2<S,T> {

  public void doSomething(S s,T t) { }

}

aspect Sub extends GenericAspect2<String,String> { }
