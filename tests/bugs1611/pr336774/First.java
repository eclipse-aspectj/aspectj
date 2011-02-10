interface NodeBacked {
//  <T extends NodeBacked> T projectTo(Class<T> cts);
}

aspect X {
  public <T extends NodeBacked> T NodeBacked.projectTo(Class<T> cts) {return null;}
}
