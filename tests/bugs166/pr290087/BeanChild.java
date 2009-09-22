public class BeanChild extends GenericParent<Bean> {
  public BeanChild(Class<? extends Bean> c) {
    super(c);
  }
  public static void main(String []argv) {
    new BeanChild(null);
  }
}

