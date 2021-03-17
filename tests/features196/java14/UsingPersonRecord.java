public class UsingPersonRecord {
  public static void main(String[] argv) {
    Person p = new Person("A","B",99);
    System.out.println(p);
    System.out.println(p.firstName());
  }
}
