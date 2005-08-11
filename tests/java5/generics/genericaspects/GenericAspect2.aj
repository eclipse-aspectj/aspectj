// error, must parameterize super aspect

public abstract aspect GenericAspect2<S,T> {

  public void doSomething(S s,T t) { }

}

aspect Sub extends GenericAspect2 { }
