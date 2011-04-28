public aspect Code2 {

  static int[] a = new int[]{1,2,3};

  static void f(){
    new Test(a[0]++);
}

after() returning(Object obj) : call(Test.new(..)) {
}

  public static void main(String []argv) {
   f();
 }
}

class Test {
  Test(int i) {}
}
