package problem;

public class SpecificService extends GenericService<Specific> {
  @Override
  protected Specific update(Specific current) {
    return null;
  }

  public static void main(String[] args) {
    new SpecificService();
  }
}
