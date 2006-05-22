

interface Result {}

interface Factory {
  Result getInstance();
}

class B   {}

class D implements Factory {}

aspect EnsureBImplementsResult {

  // bug: this should work
  declare parents: B implements Result;


  // bug: get error here wrt invalid return type
  public B D.getInstance() { 
    return new B(); 
  }
}
