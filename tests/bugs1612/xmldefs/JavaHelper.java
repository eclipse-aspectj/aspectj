public class JavaHelper {
  public static void advice1() {
    System.out.println("in advice");
  }
  public static void advice2(String s) {
    System.out.println("in advice: s="+s);
  }
  public static void advice3(String s) {
    System.out.println("in advice3: s="+s);
  }
  public static void advice4(org.aspectj.lang.JoinPoint tjp, String s) {
    System.out.println("in advice4: s="+s+" at "+tjp);
  }
  public static void advice5(org.aspectj.lang.JoinPoint tjp, String s) {
    System.out.println("in advice5: s="+s+" at "+tjp);
  }
  public static void advice6(org.aspectj.lang.JoinPoint.StaticPart tjp, String s) {
    System.out.println("in advice6: s="+s+" at "+tjp);
  }
  public static void advice7(org.aspectj.lang.JoinPoint.StaticPart tjp, String s) {
    System.out.println("in advice7: s="+s+" at "+tjp);
  }
  public static void around1(org.aspectj.lang.JoinPoint.StaticPart tjp, String s) {
    System.out.println("in around advice: s="+s+" at "+tjp);
  }
  public static int around2(org.aspectj.lang.JoinPoint.StaticPart tjp, String s) {
    System.out.println("in around2 advice: s="+s+" at "+tjp);
    return 99;
  }
  public static int around3(org.aspectj.lang.ProceedingJoinPoint pjp, String s) {
    pjp.proceed(new Object[]{"abcde"});
    return 42;
  }
  public static String around4(org.aspectj.lang.ProceedingJoinPoint pjp, String s) {
    System.out.println("around4 running");
    pjp.proceed(new Object[]{"abcde"});
    return "xyz";
  }
}

