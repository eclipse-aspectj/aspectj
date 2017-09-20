module e.f.g {
  opens com.foo1;
  opens com.foo2 to a.b.c;
  opens com.foo3 to a.b.c, b.c.d;
}
