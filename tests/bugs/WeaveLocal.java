// for Bug#:  32463  
import org.aspectj.testing.Tester;


public class WeaveLocal
{
    // Commenting out the static declaration makes everything work OK
    static
    {
        class StaticNestedClass
        {
        }
    }
    
    public static void main(String[] args)
    {
      System.out.println(new WeaveLocal().handleOrder("test"));
    }

    private String handleOrder(String t)
    {
      return t;
    }

}

aspect A  {

    pointcut withinTest(): within(WeaveLocal);
    pointcut callToHandleOrder() : (withinTest() &&
                             call(* handleOrder(..)));

    Object around(): callToHandleOrder() {

      return "DUMMY inserted by ASPECT" ;
   }
}
