aspect Test {

  pointcut p(): bean(foo*);

  before(): p() { }
}