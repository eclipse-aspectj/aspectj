import java.util.*;

public class Base {

  public int publicIntField;

  private String privateStringField;

  public List publicListField;

  protected List protectedListField;

  public static void main(String[]argv) {
    new Base().x();
  }

  public void x() {
    publicIntField     = 1;
    privateStringField = "aaa";
    publicListField = new ArrayList();
    protectedListField = new ArrayList();
  }
}
