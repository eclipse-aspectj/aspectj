public class AroundReturnType {
     public static void main(String[] args){
         new AroundReturnType().go();
     }

     void go(){
         System.out.println("... "+ s()  );
     }

     static Integer s() {
       return new Integer(1);
     }
}

aspect A {
    void around(): within(AroundReturnType) && call(Integer AroundReturnType.s()){
	System.out.println("s - advice");  
	proceed();
    }
    
    Integer around(): within(AroundReturnType) && execution(* *(..)) {
	proceed();
	return new Integer(3);
    }
  
}
