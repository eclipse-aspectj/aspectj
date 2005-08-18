import java.util.*;

public aspect WildcardArgsExamples {
	
     before(List<? extends Number> aListOfSomeNumberType) 
       : call(* foo(..)) && args(aListOfSomeNumberType) {
        System.out.println("advice match at " + thisJoinPointStaticPart);
     }
     
     before(List<? extends Number> aListOfSomeNumberType) 
	     : (call(* goo*(List<Number+>)) || call(* goo*(List<? extends Number>)))
	       && args(aListOfSomeNumberType) {
	         System.out.println("advice match 2 at " + thisJoinPointStaticPart);
     }
	
     public static void main(String[] args) {
         C c = new C();
         List<String> ls = new ArrayList<String>();
         List<Double> ld = new ArrayList<Double>();
         c.foo("hi");
         c.foo(ls);
         c.foo(ld);
         List<Number> ln = new ArrayList<Number>();
         c.goo1(ln);
         c.goo2(ld);
         c.goo3(ls);
         List<? extends Number> lsn = ln;
         c.goo4(lsn);
         List l = new ArrayList();
         c.goo5(l);
         c.goo6(new Object());
      }
}

 class C {
    
    public void foo(Object anObject) {}         
    
    public void goo1(List<Number> ln) {}
    public void goo2(List<Double> ld) {}
    public void goo3(List<String> ls) {}
    public void goo4(List<? extends Number> lsn) {}
    public void goo5(List l) {}
    public void goo6(Object o) {}
 }
 
