public aspect ITDAspect {
  @First
  @Second
  public void App.foo(Object parameter) { }

  @First
  public void App.foo(String parameter) { }

  @Second
  public int App.foo(int parameter) {
    return parameter + 3;
  }

  public void App.foo(String... parameters) { }

  @Second
  public int App.foo(int... parameters) {
    int sum = 0;
    for (int parameter : parameters)
      sum += parameter;
    return sum;
  }
}
