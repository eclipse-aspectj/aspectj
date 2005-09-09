package test;
public class VerifyErrorOnSet {

       class Node {
               int value;

               Node(int v)
               {
                       value = v;
               }
       }


       public VerifyErrorOnSet()
       {
               new Node(1);
       }

       public static void main(String[] args) {
    	   VerifyErrorOnSet l = new VerifyErrorOnSet();
       }
 
}


aspect ListAspect {

       pointcut setField(Object t) : target(t) && set(* VerifyErrorOnSet.Node+.*);
       
       before(Object t) : setField(t) {
               System.out.println("WRITE");
               // Do something with t...
       }
       
}
