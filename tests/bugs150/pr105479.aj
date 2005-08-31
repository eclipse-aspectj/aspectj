public aspect pr105479 {
  private interface Test {
    Object getId();
  }
 
  class StringTest {
    public String getId() {
      return null;
    }
  }
 
  declare parents : StringTest implements Test;
}