public class PR78021 {

       protected static Integer counter = new Integer(4);

       public static void main(String[] args) throws Exception {
         try {
           doSomething();
           System.err.println("TEST HAS PASSED");
         } catch (Exception e) {
           System.err.println("TEST HAS FAILED: Exception thrown by doSomething: " +e.getMessage());
           throw e;
         }
       }

       public static void doSomething() {
         int i = 0;
         while (i++<1) { 
           counter=null;
           try {
             counter = new Integer(4);
             // The inclusion of the next line changes the weaving !  If it is included the woven code is wrong and the exception escapes
              if (counter == null) { break; }
             commit();
           } catch (Throwable e) {
             System.err.println("Caught exception " + e);
           } finally {
             System.err.println("In finally block");
           }
         }
       }

       protected static void commit() throws MyException {
         System.err.println("Main.commit");
       }
}

class MyException extends Exception { MyException(String s,String s2) { super(s); } }

aspect SimpleExceptionThrowingAspect {
   pointcut commitOperation() : call (* PR78021+.commit(..));

   before() throws MyException : commitOperation() {
        throw new MyException("Dummy My Exception", "55102");
   }
}