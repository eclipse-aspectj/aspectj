package p;

aspect B extends Y {
  declare parents: A* implements IFace;

}

abstract aspect Y {
  public void IFace.foo() {}
}

interface IFace {}
