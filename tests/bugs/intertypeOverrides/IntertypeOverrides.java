class Super {
  public void m(){};
}

class Sub extends Super {}

aspect A {
  public static void Sub.m(){}
}
