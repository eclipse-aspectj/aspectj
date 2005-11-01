public aspect ReturnTypeTest {
  private interface Test {
    Object getId();
    int hashCode();
  }
 
  public int Test.hashCode() {
	System.out.println("in Test.hashCode()");
    return getId().hashCode();
  }
 
  declare parents : ReturnTypeTester implements Test;
}