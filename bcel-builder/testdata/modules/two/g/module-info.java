module g.h.i {
  exports com.foo1;
  exports com.foo2;
  provides com.foo1.I1 with com.foo1.C1;
  provides com.foo2.I2 with com.foo2.C2;
}
