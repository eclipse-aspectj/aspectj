abstract class Order {
     
     public void print() {  }
}
//
class SalesOrder {
}
//
abstract aspect OrderDecorator
{
   declare parents : SalesOrder extends Order;
   public void SalesOrder.print()
   {
      super.print();  //  Line 12
   }
   protected pointcut print(Order order) : target(order) && call(public void print());
}
