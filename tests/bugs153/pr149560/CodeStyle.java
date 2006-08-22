import org.aspectj.lang.*;

aspect MyAspect {
  after(): staticinitialization(*) {
    System.out.println("after initialization "+thisJoinPointStaticPart);
  }
}

public aspect CodeStyle {

  static {
  }

  public static void main(String []argv) {
    System.out.println("InstanceExists?"+Aspects.hasAspect(MyAspect.class));
  }
}
