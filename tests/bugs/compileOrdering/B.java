public abstract aspect B {
  public void C.method(Serializable s) { //error: Serializable not imported
  }
}
class C {
}
