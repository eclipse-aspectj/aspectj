public class HiddenClass implements Test {
  @Override
  public void concat(String... words) {
    System.out.println(String.join(" ", words));
  }
}
