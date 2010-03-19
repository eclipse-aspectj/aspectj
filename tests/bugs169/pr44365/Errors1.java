public aspect Errors1 {
  before(): within(is( && !is(AnonymousType)) && staticinitialization(*) {}
}
