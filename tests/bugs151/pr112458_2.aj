public class pr112458_2<V>
{
    public void setInnerClasses(InnerClass[] classes){};

    public static class InnerClass {}

   public static void main(String []argv) {
     new pr112458_2();
   }
}

aspect X {
  before(pr112458_2.InnerClass[] ics): execution(void setInnerClasses(..)) && args(ics) {
  }
}
