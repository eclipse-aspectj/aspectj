public class C<O extends Number> {
  
  O m1() {return null;}
  void m2(O o) {}
  void m3(String s,O o) {}
  void m4(O o,O o2) {}
  O m5(O o,O o2) {return null;}

}

class D1<P extends Float> extends C<P> {

  @Override
  P m1() {return null;}

  @Override
  void m2(P s) {}

  @Override
  void m3(String s,P o) {}

  @Override
  void m4(P o,P o2) {}

  @Override
  P m5(P o,P o2) {return null;}
  
}


class D2 extends C {
 
  @Override
  Float m1() { return null; }
}
