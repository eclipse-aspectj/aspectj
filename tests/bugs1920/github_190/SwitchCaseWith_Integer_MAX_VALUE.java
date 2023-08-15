public class SwitchCaseWith_Integer_MAX_VALUE {
  public static void main(String[] args) {
    System.out.println(switchTest(Integer.MAX_VALUE));
  }

  static String switchTest(int i) {
    switch (i) {
      case Integer.MAX_VALUE:
        return "CASE_1";
      default:
        return "";
    }
  }
}

aspect MyAspect {
  before() : execution(* switchTest(*)) {
    System.out.println(thisJoinPoint);
  }
}
