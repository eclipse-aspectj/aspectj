aspect A {
  public abstract Object I.clone();
  public abstract Object I.ordinary();
}

interface I { }

interface I2 {
  public abstract Object clone();
  public abstract Object ordinary();
}


class Impl implements I {
  public Object clone() { return this;}
  public Object ordinary() { return this;}
}

class Impl2 implements I2 {
  public Object clone() { return this;}
  public Object ordinary() { return this;}
}
