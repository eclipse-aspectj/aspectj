class Super {
  public static void m(){};
}

class Sub extends Super {}

aspect A {
  public void Sub.m(){}
}
