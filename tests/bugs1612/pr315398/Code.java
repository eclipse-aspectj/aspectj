public aspect Code {

  static int[] a = new int[]{1,2,3};

  static void f(){
    System.out.println(a[0]);
    new Test(a[0]++);
    System.out.println(a[0]);
  }

  before(): call(Test.new(..)) {
    System.out.println("advice");
  }

  public static void main(String []argv) {
   f();
 }
}

class Test {
  Test(int i) {}
}
