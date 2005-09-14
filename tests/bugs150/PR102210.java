import java.util.*;

public aspect PR102210 {

 pointcut complex(List list):
   (execution(public * *(String, List)) && args(*,list)) ||
   (execution(public * *(String, List, String)) && args(*,list,*)) ||
   (execution(public * *(String, String[], List, String)) && args(*,*,list,*));

  before(List l): complex(l) {
    System.err.println("List size is "+l.size());
  }

  public static void main(String []argv) {
    List l = new ArrayList();
    l.add(".");
    m1("xxx",l);
    l.add(".");
    m2("xxx",l,"yyy");
    l.add(".");
    m3("xxx",new String[]{"xxx","yyy"},l,"zzz");
  }

  public static void m1(String a,List b) { 
    System.err.println("m1 running"); 
  }
  public static void m2(String a,List b,String c) { 
    System.err.println("m2 running"); 
  }
  public static void m3(String a,String[] b,List c,String d) { 
    System.err.println("m3 running");
  }

}
