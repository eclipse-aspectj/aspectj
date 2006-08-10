public aspect X {
  declare parents: @Ann * extends I1,Mixin;
  int Mixin.i;
  public void Mixin.m() {}
}
