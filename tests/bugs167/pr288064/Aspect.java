package test;
aspect Aspect {
  before() : call(void test..*(..))  {
    System.out.println("advice");
  }
}

