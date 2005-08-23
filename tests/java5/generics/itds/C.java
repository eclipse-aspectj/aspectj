// Using type parameter in ITD 
public abstract aspect C<T> {
  private T Foo.data;

  public T Foo.getData(T defaultValue) {
    return (this.data!=null?data:defaultValue);
  }
}

public aspect C<String> { }

class Foo {
}
