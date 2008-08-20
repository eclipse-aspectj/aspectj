package bar;

public abstract aspect PrintAround {

       abstract pointcut method();

       Object around(): method() {
               System.out.println("-before-");
               Object r = proceed();
               System.out.println("-after-");
               return r;
       }

}
