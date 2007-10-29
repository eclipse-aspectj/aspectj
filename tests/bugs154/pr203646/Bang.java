// fails

interface I {
  interface J< T > {}
}

public aspect Bang {
 public void I.J< T >.intro() {}
}