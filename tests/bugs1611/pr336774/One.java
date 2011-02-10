interface NodeBacked2 {
  Object projectTo(Class cts);
}

aspect X {
  public Object NodeBacked2.projectTo(Class cts) {return null;}
}
