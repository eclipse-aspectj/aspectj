aspect MyAspect
{
   public interface MyInterface
   {
       public boolean instanceOf(Class<? extends Object> c);
       //public boolean instanceOf(Class c);
   }

   declare parents: MyObject implements MyInterface;

   public boolean MyInterface.instanceOf(Class<? extends Object> c)
   //public boolean MyInterface.instanceOf(Class c)
   {
       return c.isInstance(this);
   }
}

class MyObject
{
  // public boolean instanceOf(Class<? extends Object> c) {return true;}
}

public class Main
{
   public static void main(String[] args)
   {
       new MyObject().instanceOf(Object.class);
   }
}
