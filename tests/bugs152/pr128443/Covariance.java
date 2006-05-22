interface Result {}
interface Factory {
    Result getInstance();
}   


aspect A_forB {
  declare parents: B implements Result;

  public B D.getInstance() { 
    return new B(); 
  }
}

class D implements Factory {}
class B   {}
