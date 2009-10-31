package com.foo;

class CCC implements CCC2  {
  public void CCCone() {}
  public void CCC2one() {}
  public void CCC3one() {}
}
  
interface CCC2 extends CCC3 {
  public void CCC2one();
}

interface CCC3 {
  public void CCC3one();
}
