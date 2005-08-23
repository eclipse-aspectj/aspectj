public abstract aspect D<T> {
              
  private T Goo<T>.data;
            
  public T Goo<T>.getData(T defaultValue) {
    return (this.data != null ? data : defaultValue);
  }   
                
}

aspect E extends D<String> {}

class Goo<P> {}
