public class TypeAnnoOnNew {
  public void m() {
    Object o = new @Anno String();
    Object o2= new @Anno(2) String[1];
    Object o3 = new @Anno(3) int @Anno(4)[3][3];
  }
}
