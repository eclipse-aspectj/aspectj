public class SizeIssues {

  public static void main(String[] argv) {
	int foo1 = 1;
	String foo2 = "2";
	Integer foo3 = new Integer(3);
	String foo4 = "4";
	callfoo(foo1,foo2,foo3,foo4);
  }

  public static void callfoo(int input1,String input2, Integer input3,String input4) {
	bar_1(input1);
	bar_2(input2);
	bar_1(input3.intValue());
	bar_2(input4);
  }

  public static void bar_1(int i) {}
  public static void bar_2(String s) {}

}