public aspect pr114744 {
  Class<? extends pr114744> pr114744.cl;

  void foo() throws Exception {
    pr114744 ci = cl.newInstance();
  }
}
