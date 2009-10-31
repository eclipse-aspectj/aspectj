package com.foo;

class CC extends CC2  {
  public void CCone() {}
  public void CC3two() {}
}

abstract class CC2 implements CC3 {
  public void CC2one() {}
  public void CC3one() {}
}
  
interface CC3 {
  public void CC3one();
  public void CC3two();
}
