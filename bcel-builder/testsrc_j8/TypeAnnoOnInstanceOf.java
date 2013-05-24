public class TypeAnnoOnInstanceOf {
  public void m() {
    Object o = "abc";
    if (o instanceof @Anno(1) String) {
      String s = (String) o;
    }
    if (o instanceof @Anno(1) Integer) {
      Integer s = (Integer) o;
    }
  }
}
