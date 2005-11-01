public aspect ReturnTypeTest {
  private interface Test {
    Object getId();
    int hashCode();
  }
 
  public int Test.hashCode() {
    return getId().hashCode();
  }
 
  declare parents : ReturnTypeTester implements Test;
}
